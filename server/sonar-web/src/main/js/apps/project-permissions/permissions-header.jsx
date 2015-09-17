import React from 'react';

export default React.createClass({
  propTypes: {
    permissions: React.PropTypes.arrayOf(React.PropTypes.object).isRequired
  },

  render() {
    let cellWidth = (80 / this.props.permissions.length) + '%';
    let cells = this.props.permissions.map(p => {
      return (
          <th key={p.key} colSpan="2" className="text-top" style={{ width: cellWidth }}>
            <div className="table-list-cell">
              <h4>{p.name}</h4>
              <p className="note little-spacer-top">{p.description}</p>
            </div>
          </th>
      );
    });
    return (
        <thead>
        <tr>
          <th style={{ width: '20%' }}>&nbsp;</th>
          {cells}
          <th className="thin">&nbsp;</th>
        </tr>
        </thead>
    );
  }
});
