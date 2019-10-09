import React from 'react'
import { Row, Col, Button } from 'react-bootstrap'
import { BackgroundJobInfo, AppState } from '../models/index';
import { Action } from '../redux/index';
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
//@ts-ignore
import Octicon from 'react-octicon'

interface Props {
  msg?: string
  display?: boolean
  cancelCallback?: () => void
}

class LoadingOverlayCon extends React.Component<Props,any> {
  private cancel() {
    //TODO: implement a better cancelation strategy .. e.g.: abort the running promise, fetch call?!
    if(window.confirm("Cancelation a background job is dangerous and experimental. Are you sure?")) {
      (this.props.cancelCallback) ? this.props.cancelCallback() : window.location.reload()
    }
  }

  render() {
    const displayStr = (this.props.display) ? 'block' : 'none'
    return (
      <div id="overlay" style={{display: displayStr}}>
        <div id="overlay-close">
          <Button variant="link" className="text-danger" onClick={() => this.cancel()}><Octicon name="x" style={{fontSize: '24pt'}} /></Button>
        </div>
        <div id="overlay-content">
          <div className="spinner-border text-info" role="status" style={{width: '15rem', height: '15rem'}}>
          </div>
          <div>
            {this.props.msg || "Loading, please be patient."}
          </div>
        </div>
      </div>
    )
  }
}

export function withOverlay<A>(setBgInfo: (running: boolean, msg: string | undefined) => Action, msg?:string): (p:Promise<A>) => Promise<A> {
  return (p:Promise<A>) => {
    setBgInfo(true, msg)
    return p.finally(() => setBgInfo(false, undefined))
  }
}

function mapToProps(state: AppState) {
  return {
    msg: state.jobInfo.message,
    display: state.jobInfo.running
  }
}

export const LoadingOverlay = connect(mapToProps, null)(LoadingOverlayCon)
