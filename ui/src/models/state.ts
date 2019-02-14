
import {Project} from './project'
import {File} from './file'

export interface UserAuth {
  username:string,
  jwtToken:string
}

export interface Session {
  files: File[],
  openedFiles: File[]
}

export interface AppState {
  authentication: UserAuth | undefined,
  projects:Project[],
  session: Session
}

export function initialState(): AppState {
  return {
    authentication: undefined,
    projects:[],
    session: {
      files: [],
      openedFiles: []
    }
  }
}
