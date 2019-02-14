export enum ActionTypes {
    Login,
    Logout
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const login: () => Action = () => { return {type: ActionTypes.Login} }
