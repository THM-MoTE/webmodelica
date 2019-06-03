import React from 'react'
import { Row, Col } from 'react-bootstrap'

interface Props {
  msg?: string
  display?: boolean
}

export class LoadingOverlay extends React.Component<Props,any> {
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
