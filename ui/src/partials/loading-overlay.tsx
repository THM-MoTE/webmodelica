import React from 'react'
import { Row, Col } from 'react-bootstrap'
import { BackgroundJobInfo, AppState } from '../models/index';
import { Action } from '../redux/index';
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'

interface Props {
  msg?: string
  display?: boolean
}

class LoadingOverlayCon extends React.Component<Props,any> {
  render() {
    const displayStr = (this.props.display) ? 'block' : 'none'
    return (
      <div id="overlay" style={{display: displayStr}}>
        <div id="overlay-content">
          <div className="spinner-border text-info" role="status" style={{width: '15rem', height: '15rem'}}>
          </div>
          <div>
            {this.props.msg || "Loading be patient.."}
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
