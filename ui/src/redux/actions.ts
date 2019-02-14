import {UserAuth} from '../models/state'
import {Project} from '../models/project'
import {File} from '../models/file'

export enum ActionTypes {
    Login,
    Logout,
    SetProjects,
    UpdateSessionFiles
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const login: (pl:UserAuth) => Action = (pl:UserAuth) => { return {type: ActionTypes.Login, payload: pl} }
export const setProjects: (ps:Project[]) => Action = (ps:Project[]) => { return {type: ActionTypes.SetProjects, payload: ps} }
export const updateSessionFiles: (fs:File[]) => Action = (fs:File[]) => ({type:ActionTypes.UpdateSessionFiles, payload: fs})
