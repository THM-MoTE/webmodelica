import { UserAuth, Project, File, Session } from '../models/index'

export enum ActionTypes {
  Logout,
  SetProjects,
  AddProject,
  UpdateSessionFiles,
  SetSession,
  UpdateWsToken,
  CreateNewFile
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const updateToken: (pl: String) => Action = pl => ({ type: ActionTypes.UpdateWsToken, payload: pl })
export const addProject = (p: Project) => ({ type: ActionTypes.AddProject, payload: p })
export const setProjects: (ps: Project[]) => Action = (ps: Project[]) => { return { type: ActionTypes.SetProjects, payload: ps } }
export const updateSessionFiles: (fs: File[]) => Action = (fs: File[]) => ({ type: ActionTypes.UpdateSessionFiles, payload: fs })
export const setSession = (s: Session) => ({ type: ActionTypes.SetSession, payload: s })

export const newFile = (f: File) => ({ type: ActionTypes.CreateNewFile, payload: f })
