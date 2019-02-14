import {Action} from './actions'

export function defaultMapDispatchToProps(dispatch: (a:Action) => void) {
  return {
    dispatch: dispatch
  };
}
