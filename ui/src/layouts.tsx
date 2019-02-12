import React, { Component } from 'react';

export function Container(props:any) {
  return (
    <div className="container-fluid">
      {props.children}
    </div>
  )
}
