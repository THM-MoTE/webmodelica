import {Action} from './actions'
import {AppState} from '../models/state'

export function defaultMapDispatchToProps(dispatch: (a:Action) => void) {
  return {
    dispatch: dispatch
  };
}

export function mapAuthenticationToProps(state:AppState) {
  return {
    authentication: state.authentication
  }
}
