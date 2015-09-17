import React from 'react';
import PermissionUsers from './permission-users';
import PermissionGroups from './permission-groups';

let $ = jQuery;

// Maximum number of displayed groups
const MAX_ITEMS = 3;

export default React.createClass({
  propTypes: {
    permission: React.PropTypes.object.isRequired
  },

  render() {
    return (
        <tr data-id={this.props.permission.key}>
          <td className="text-top">
            <div className="table-list-cell">
              <h3>{this.props.permission.name}</h3>
              <p className="spacer-top" dangerouslySetInnerHTML={{ __html: this.props.permission.description }}/>
            </div>
          </td>
          <td className="text-middle thin nowrap">
            <PermissionUsers permission={this.props.permission}
                             project={this.props.project}
                             refresh={this.props.refresh}/>
          </td>
          <td className="text-middle thin nowrap">
            <PermissionGroups permission={this.props.permission}
                              project={this.props.project}
                              refresh={this.props.refresh}/>
          </td>
        </tr>
    );
  }
});
