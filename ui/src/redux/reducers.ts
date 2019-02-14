import {initialState, AppState} from '../models/state'
import {Project} from '../models/project'
import {Action, ActionTypes} from './actions'

const reducerMap = {
  [ActionTypes.Login.toString()]: (state:AppState, data:any) => { return {...state, authentication: data} },
  [ActionTypes.SetProjects.toString()]: (state:AppState, data:Project[]) => { return {...state, projects: data} }
}

export function rootReducer(state:AppState=initialState(), action:Action):AppState {
  const fn = reducerMap[action.type.toString()]
  if(fn) {
    return fn(state, action.payload)
  } else {
    console.error("no reducer applied to", action)
    return state
  }
}
