import { UserAuth, Project, File } from '../models/index'

export enum ActionTypes {
  Login,
  Logout,
  SetProjects,
  AddProject,
  UpdateSessionFiles
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const login: (pl: UserAuth) => Action = (pl: UserAuth) => { return { type: ActionTypes.Login, payload: pl } }
export const addProject = (p: Project) => ({ type: ActionTypes.AddProject, payload: p })
export const setProjects: (ps: Project[]) => Action = (ps: Project[]) => { return { type: ActionTypes.SetProjects, payload: ps } }
export const updateSessionFiles: (fs: File[]) => Action = (fs: File[]) => ({ type: ActionTypes.UpdateSessionFiles, payload: fs })
