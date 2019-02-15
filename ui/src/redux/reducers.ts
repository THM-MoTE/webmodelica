import { initialState, AppState } from '../models/state'
import { Project } from '../models/project'
import { File } from '../models/file'
import { Action, ActionTypes } from './actions'
import * as R from 'ramda'

const reducerMap = {
  [ActionTypes.Login.toString()]: (state: AppState, data: any) => { return { ...state, authentication: data } },
  [ActionTypes.SetProjects.toString()]: (state: AppState, data: Project[]) => { return { ...state, projects: data } },
  [ActionTypes.UpdateSessionFiles.toString()]: (state: AppState, data: File[]) => ({ ...state, session: { ...state.session, files: data } }),
  [ActionTypes.AddProject.toString()]: (state: AppState, data: Project) => ({ ...state, projects: R.append(data, state.projects) })
}

export function rootReducer(state: AppState = initialState(), action: Action): AppState {
  const fn = reducerMap[action.type.toString()]
  if (fn) {
    let st = fn(state, action.payload)
    console.log("reducer-state", st)
    return st
  } else {
    console.error("no reducer applied to", action)
    return state
  }
}
