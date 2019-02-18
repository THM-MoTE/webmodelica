
import { File, Project, TokenWrapper, Session } from '../models'
import React, { Component } from 'react';

function rejectError(res: Response): Promise<Response> {
  if (res.ok) return Promise.resolve(res)
  else {
    return res.text().then(txt => Promise.reject(txt || res.statusText))
  }
}

const authHeader = "Authorization"

export class ApiClient {

  private base: string
  private token?: string

  constructor(baseUri: string) {
    this.base = baseUri
    this.token = undefined
  }

  private userUri(): string {
    return this.base + "users"
  }
  private projectUri(): string {
    return this.base + "projects"
  }

  private updateWSToken(res: Response): Response {
    //TODO: consider saving the token into store directly in here..
    //TODO: provide the store to this client ..
    const headerOpt = res.headers.get(authHeader)
    this.token = headerOpt || this.token
    console.log("new token is:", this.token)
    return res
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
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
  }

  public projects(): Promise<Project[]> {
    return fetch(this.projectUri(), {
      method: 'GET',
      headers: {
        'Authentication': this.token!,
        'Accept': 'application/json'
      }
    })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
  }

  public newProject(user: string, title: string): Promise<Project> {
    if (user.length > 0 && title.length > 0) {
      return fetch(this.projectUri(), {
        method: 'POST',
        headers: {
          'Authentication': this.token!,
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
    return fetch(this.projectUri() + `/${project.id}/sessions/new`, {
      method: 'POST',
      headers: {
        'Authentication': this.token!,
        'Accept': 'application/json'
      }
    })
      .then(rejectError)
      .then(this.updateWSToken.bind(this))
      .then(res => res.json())
      .then((s: Session) => {
        //TODO: use files from backend!
        s.files = [{ relativePath: "a/b/simple.mo", content: "simple" },
        { relativePath: "factor.mo", content: "function factor end factor;" }]
        return s
      }
      )
  }
}

export const defaultClient = new ApiClient(window.location.toString())
