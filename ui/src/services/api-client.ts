
import { File, Project, TokenWrapper } from '../models'
import React, { Component } from 'react';

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

  public login(user: string, pw: string): Promise<TokenWrapper> {
    // return fetch(this.userUri()+"/login", {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //     "Accept": 'application/json'
    //   },
    //   body: JSON.stringify({username: user, password: pw})
    // })
    // .then(res => res.json())
    return Promise.resolve({ token: "abcdef" })
  }

  public projects(): Promise<Project[]> {
    // return fetch(this.projectUri(), {
    //   method: 'GET',
    //   headers: {
    //     'Accept': 'application/json'
    //   }
    // })
    // .then(res => res.json())
    return Promise.resolve([
      { id: "123456", name: "Project 1", owner: "Nico" },
      { id: "23456", name: "Project 2", owner: "Nico" },
      { id: "11-11-b", name: "Project 3", owner: "Nico" }
    ])
  }

  public newProject(user: string, title: string): Promise<Project> {
    if (user.length > 0 && title.length > 0)
      return Promise.resolve({ owner: user, name: title, id: "aabbcc" })
    else
      return Promise.reject("username & title must be provided and not empty!")
  }

  public getFiles(): Promise<File[]> {
    return Promise.resolve([
      { relativePath: "a/b/simple.mo", content: "simple" },
      { relativePath: "factor.mo", content: "function factor end factor;" }
    ])
  }
}

export const defaultClient = new ApiClient(window.location.toString())
