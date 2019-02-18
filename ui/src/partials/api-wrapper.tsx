import React, { Component } from 'react';
import { ApiClient } from '../services/api-client';

export function withApi(client: ApiClient, WrappedComponent: any) {
  return class ApiWrapper extends Component<any, any> {
    public render() {
      return (<WrappedComponent {...this.props} api={client} />)
    }
  }
}
