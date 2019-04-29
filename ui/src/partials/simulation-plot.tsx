import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Row, Col, Button } from 'react-bootstrap'
import { AppState, Session, TableFormat } from '../models/index'
import { Chart } from 'react-google-charts'
import { ApiClient } from '../services/api-client'
import { Action } from '../redux/actions'
import * as R from 'ramda';

interface Props {
  api: ApiClient
  data: TableFormat
  address:URL
}
type State = any

class SimulationPlotCon extends React.Component<Props, State> {
  private readonly chartOptions:any
  constructor(p: Props) {
    super(p)
    const dataSet = R.prepend(p.data.header as any[], p.data.data as any[][])
    this.state = { dataSet: dataSet }
    this.chartOptions = {
      //display edges as soft curves, not hard edges
      //curveType: 'function'
    }
  }
  render() {
    const csvUrl = new URL('', this.props.address)
    csvUrl.searchParams.set("format", "csv")
    return (<><Row>
      <Col xs={10}>
        <Chart chartType="LineChart" height="80vh" data={this.state.dataSet} legendToggle options={this.chartOptions}/>
      </Col>
      <Col>
        <h5 className="text-secondary">Plot Actions</h5>
        <Button variant="outline-primary" href={csvUrl.toString()}>Download CSV</Button>
      </Col>
    </Row></>)
  }
}

function mapProps(state: AppState) {
  return {}
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

const SimulationPlot = connect(mapProps, dispatchToProps)(SimulationPlotCon)
export default SimulationPlot
