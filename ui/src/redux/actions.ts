import {UserAuth} from '../models/state'
import {Project} from '../models/project'

export enum ActionTypes {
    Login,
    Logout,
    SetProjects
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const login: (pl:UserAuth) => Action = (pl:UserAuth) => { return {type: ActionTypes.Login, payload: pl} }
export const setProjects: (ps:Project[]) => Action = (ps:Project[]) => { return {type: ActionTypes.SetProjects, payload: ps} }
