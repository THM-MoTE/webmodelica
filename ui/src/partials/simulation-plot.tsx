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
}
type State = any

class SimulationPlotCon extends React.Component<Props, State> {

  constructor(p: Props) {
    super(p)
    const dataSet = R.prepend(p.data.header as any[], p.data.data as any[][])
    this.state = { dataSet: dataSet }
  }

  render() {
    console.log("dataset: ", this.state.dataSet)
    return (
      <Chart chartType="LineChart" width='800px' height='600px' data={this.state.dataSet} legendToggle />
    )
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
