
import { File, Project, TokenWrapper, Session, AppState, UserAuth, CompilerError, SimulationResult, TableFormat, SimulateRequest, Complete, Suggestion, AppInfo } from '../models/index'
import React, { Component } from 'react';
import { Store } from 'redux';
import { updateToken } from '../redux/index';
import * as R from 'ramda'
import { Uri } from 'monaco-editor';

function rejectError(res: Response): Promise<Response> {
  if (res.ok) return Promise.resolve(res)
  else {
    console.error("api error:", res)
    return res.text().then(txt => Promise.reject(txt || res.statusText))
  }
}

const authHeader = "Authorization"
const apiPrefix = "/api/v1/webmodelica/"

const backendUri: () => string =
  () => window.location.protocol + "//" + window.location.host + apiPrefix

export function fetchAppInfos(): Promise<AppInfo> {
  return fetch(backendUri()+"info", {
    method: 'GET',
    headers: {
      "Accept": 'application/json'
    }
  })
  .then(res => res.json())
}

export class ApiClient {
  private readonly base: string
  private readonly store: Store<AppState>

  constructor(store: Store<AppState>) {
    this.store = store
    this.base = backendUri()
    window.addEventListener('beforeunload', (e: BeforeUnloadEvent) => this.deleteCurrentSession())
  }

  private userUri(): string {
    return this.base + "users"
  }
  private projectUri(): string {
    return this.base + "projects"
  }
  private sessionUri(): string {
    return this.base + "sessions"
  }

  private updateWSToken(res: Response): Response {
    const headerOpt = res.headers.get(authHeader)
    if (headerOpt) {
      document.cookie = `Authorization=${headerOpt}; path=/;`
      this.store.dispatch(updateToken(headerOpt))
    }
    return res
  }
  private token(): string { return this.store.getState().authentication!.token.raw }

  private withSession<A>(err:string): Promise<Session> {
    const session = this.store.getState().session
    if (session) {
      return Promise.resolve(session)
    } else {
      return Promise.reject(err)
    }
  }

  public projectDownloadUrl(id:string): string {
    return this.projectUri() + `/${id}/files/download`
  }

  public login(user: string, pw: string): Promise<TokenWrapper> {
    return fetch(this.userUri() + "/login", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        "Accept": 'application/json'
      },
      body: JSON.stringify({ username: user, password: pw })
    })
      .then(rejectError)
      .then(res => res.json())
      .then((t: TokenWrapper) => {
        this.store.dispatch(updateToken(t.token))
        return t
      })
  }

  public projects(): Promise<Project[]> {
    return fetch(this.projectUri(), {
      method: 'GET',
      headers: {
        [authHeader]: this.token(),
        'Accept': 'application/json'
      }
    })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
  }

  public projectFiles(pid:string): Promise<File[]> {
    return fetch(this.projectUri()+`/${pid}/files`,{
      headers: {
        [authHeader]: this.token(),
        'Accept': 'application/json',
      },
      })
      .then(res => res.json())
  }

  public newProject(user: string, title: string): Promise<Project> {
    if (user.length > 0 && title.length > 0) {
      return fetch(this.projectUri(), {
        method: 'POST',
        headers: {
          [authHeader]: this.token(),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ owner: user, name: title })
      })
        .then(rejectError)
        .then(this.updateWSToken.bind(this))
        .then(res => res.json())
    }
    else
      return Promise.reject("username & title must be provided and not empty!")
  }

  public newSession(project: Project): Promise<Session> {
    this.deleteCurrentSession()
    return fetch(this.projectUri() + `/${project.id}/sessions/new`, {
      method: 'POST',
      headers: {
        [authHeader]: this.token(),
        'Accept': 'application/json'
      }
    })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
      .then((obj: any) => ({
        ...obj, compilerErrors: [], simulation: { options: [
          { name: "startTime", value: 0 },
          { name: "stopTime", value: 5 },
          { name: "numberOfIntervals", value: 500 }
        ],
        data: []
        }
      }))
  }

  public deleteSession(sid:string): Promise<void> {
    console.log("deleting session:", sid)
    return fetch(this.sessionUri()+`/${sid}`, {
      method: 'DELETE',
      headers: {
        [authHeader]: this.token(),
      }
    })
    .then(rejectError)
    .then(_ => undefined)
  }

  public deleteCurrentSession(): Promise<void> {
    const session = this.store.getState().session
    if(session) return this.deleteSession(session.id)
    else return Promise.resolve(undefined)
  }

  public copyProject(p:Project, newName?:string): Promise<Project> {
    //POST / api / v1 / projects /: projectId / copy
    return fetch(this.projectUri()+`/${p.id}/copy`, {
      method: 'POST',
      headers: {
        [authHeader]: this.token(),
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: (newName) ? JSON.stringify({ name: newName }) : undefined
    })
    .then(rejectError)
    .then(this.updateWSToken.bind(this))
    .then(res => res.json())
  }

  public deleteProject(p:Project | string): Promise<void> {
    const id = (typeof p == "string") ? p : p.id
    return fetch(this.projectUri() + `/${id}`, {
      method: 'DELETE',
      headers: {
        [authHeader]: this.token(),
      }
    })
    .then(rejectError)
    .then(this.updateWSToken.bind(this))
    .then(_ => undefined)
  }

  public updateVisibility(pId:string, visibility:string): Promise<Project> {
    return fetch(this.projectUri()+`/${pId}/visibility`, {
      method: 'PUT',
      headers: {
        [authHeader]: this.token(),
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({visibility})
    })
    .then(rejectError)
    .then(this.updateWSToken.bind(this))
    .then(res => res.json())
  }

  public compile(file: File): Promise<CompilerError[]> {
    const session = this.store.getState().session
    if (session) {
      return fetch(this.sessionUri() + `/${session.id}/compile`, {
        method: 'POST',
        headers: {
          [authHeader]: this.token(),
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({ path: file.relativePath })
      }).then(rejectError)
        .then(this.updateWSToken.bind(this))
        .then(res => res.json())
    } else {
      return Promise.reject("can't compile if there is no session!")
    }
  }

  public updateFile(file: File): Promise<File> {
    const session = this.store.getState().session
    if (session) {
      return fetch(this.sessionUri() + `/${session.id}/files/update`, {
        method: 'POST',
        headers: {
          [authHeader]: this.token(),
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(file)
      })
        .then(rejectError)
        .then(this.updateWSToken.bind(this))
        .then(res => file)
    } else {
      return Promise.reject("can't create a file if there is no session!")
    }
  }

  public deleteFile(file:File): Promise<void> {
    return this.withSession("can't delete a file if there is no session!")
      .then(session =>  {
        const url = new URL(this.sessionUri() + `/${session.id}/files`)
        url.searchParams.set("path", file.relativePath)
        return fetch(url.toString(), {
          method: 'DELETE',
          headers: {
            [authHeader]: this.token()
          }
        })
      })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(_ => {})
  }

  public renameFile(file:File, name:string): Promise<File> {
    return this.withSession("can't rename a file if there is no session!")
      .then(session => {
        const data = {oldPath: file.relativePath, newPath: name}
        return fetch(this.sessionUri() + `/${session.id}/files/rename`, {
          method: 'PUT',
          headers: {
            [authHeader]: this.token(),
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify(data)
        })
      })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
  }

  public uploadArchive(f:any): Promise<File[]> {
    return this.withSession("can't upload archive if there is no session!")
      .then(session => {
        const data = new FormData()
        data.append("archive", f)
        return fetch(this.sessionUri() + `/${session.id}/files/upload`, {
          method: 'POST',
          headers: {
            [authHeader]: this.token(),
            'Accept': 'application/json'
          },
          body: data
        })
      })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
  }

  public simulate(r:SimulateRequest): Promise<string> {
    const session = this.store.getState().session
    if (session) {
      return fetch(this.sessionUri() + `/${session.id}/simulate`, {
        method: 'POST',
        headers: {
          [authHeader]: this.token(),
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(r)
      })
        .then(rejectError)
        .then(this.updateWSToken.bind(this))
        .then(res => res.headers.get("Location")!)
    } else {
      return Promise.reject("can't simulate without a session!")
    }
  }

  public getSimulationResults(addr: URL, format: string = "chartjs"): Promise<SimulationResult | TableFormat> {
    const session = this.store.getState().session
    if (session) {
      addr.searchParams.set("format", format)
      return fetch(addr.toString(), {
        method: 'GET',
        headers: {
          [authHeader]: this.token(),
          'Accept': 'application/json'
        }
      })
        .then(rejectError)
        .then(this.updateWSToken.bind(this))
        .then(res => res.json())
    } else {
      return Promise.reject("can't simulate without a session!")
    }
  }

  public autocomplete(c: Complete): Promise<Suggestion[]> {
    return this.withSession("can't complete without a session")
      .then(session =>
        fetch(this.sessionUri()+`/${session.id}/complete`, {
          method: 'POST',
          headers: {
            [authHeader]: this.token(),
            'Accept': 'application/json',
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(c)
        })
      )
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
      .then(lst => lst.map((s:Suggestion) => ({ ...s, kind: s.kind.toLowerCase()})))
  }
}
