import {initialState, AppState} from '../models/state'
import {Action, ActionTypes} from './actions'

const reducerMap = {
  [ActionTypes.Login.toString()]: (state:AppState, data:any) => { return {...state, authentication: data} }
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
