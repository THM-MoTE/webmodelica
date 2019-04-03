import React from 'react'
import { Row, Col } from 'react-bootstrap'

interface Props {
  msg:string
  display:boolean
}

export class LoadingSpinner extends React.Component<Props, any> {
  render() {
    return (<>
      {this.props.display && (<><Row>
        <Col className="d-flex justify-content-center">
          <div className="spinner-border text-info" role="status">
          </div>
        </Col>
      </Row>
        <Row>
          <Col className="d-flex justify-content-center">{this.props.msg}</Col>
      </Row></>) }
      </>
    )
  }
}
