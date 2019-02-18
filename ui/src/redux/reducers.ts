import { initialState, AppState, Session } from '../models/state'
import { Project } from '../models/project'
import { File } from '../models/file'
import { Action, ActionTypes } from './actions'
import * as R from 'ramda'

const reducerMap = {
  [ActionTypes.Login.toString()]: (state: AppState, data: any) => { return { ...state, authentication: data } },
  [ActionTypes.SetProjects.toString()]: (state: AppState, data: Project[]) => { return { ...state, projects: data } },
  [ActionTypes.UpdateSessionFiles.toString()]: function(state: AppState, data: File[]): AppState {
    if (state.session) {
      return { ...state, session: { ...state!.session, files: data } }
    } else {
      console.error("can't set session files if no session provided before!")
      return state
    }
  },
  [ActionTypes.AddProject.toString()]: (state: AppState, data: Project) => ({ ...state, projects: R.prepend(data, state.projects) }),
  [ActionTypes.SetSession.toString()]: (state: AppState, session: Session) => ({ ...state, session: session })
}

export function rootReducer(state: AppState = initialState(), action: Action): AppState {
  const fn = reducerMap[action.type.toString()]
  if (fn) {
    let st = fn(state, action.payload)
    return st
  } else {
    console.error("no reducer applied to", action)
    return state
  }
}
