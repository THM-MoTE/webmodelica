import React from 'react'
import { Alert } from 'react-bootstrap'
import * as R from 'ramda'

export function renderErrors(errors: string[]) {
  return (<>{!R.isEmpty(errors) &&
    (<Alert variant="danger">
      <ul>
      {errors.map((e,idx) => (<li key={idx}>{e}</li>))}
      </ul>
    </Alert>)
  }</>)
}
