import {UserAuth} from '../models/state'

export enum ActionTypes {
    Login,
    Logout
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const login: (pl:UserAuth) => Action = (pl:UserAuth) => { return {type: ActionTypes.Login, payload: pl} }
