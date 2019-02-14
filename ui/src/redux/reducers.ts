import {initialState, AppState} from '../models/state'
import {Action, ActionTypes} from './actions'

export function rootReducer(state:AppState=initialState(), action:Action):AppState {
  console.log("state", state, "action", action)
  switch(action.type) {
    case ActionTypes.Login:
      state.authentication = {username: "nico", jwtToken: "12345"}
      break;
  }
  return state
}
