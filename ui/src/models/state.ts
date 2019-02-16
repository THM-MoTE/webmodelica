
import { File, Project } from './index'

export interface UserAuth {
  username: string,
  jwtToken: string
}

export interface Session {
  project: Project,
  id: string,
  files: File[],
  openedFiles: File[]
}

export interface AppState {
  authentication?: UserAuth
  projects: Project[],
  session?: Session
}

export function initialState(): AppState {
  return {
    authentication: undefined,
    projects: [],
    session: undefined
  }
}
