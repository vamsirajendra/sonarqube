import React from 'react';
import Permission from './permission';

export default React.createClass({
  propTypes:{
    permissions: React.PropTypes.arrayOf(React.PropTypes.object).isRequired
  },

  renderPermissions() {
    return this.props.permissions.map(permission => {
      return <Permission key={permission.key} permission={permission} project={this.props.project}
                         refresh={this.props.refresh}/>;
    });
  },

  render() {
    return (
        <table className="table-list" id="global-permissions-list">
          <tbody>{this.renderPermissions()}</tbody>
        </table>
    );
  }
});
