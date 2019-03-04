
import { CompilerError, File, Project } from './index'

export interface UserAuth {
  username: string,
  token: JwtToken
}

export interface JwtToken {
  username: string,
  issued: Date,
  expires: Date,
  raw: string
}

export interface Session {
  project: Project,
  id: string,
  files: File[],
  openedFiles: File[],
  compilerErrors: CompilerError[]
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
