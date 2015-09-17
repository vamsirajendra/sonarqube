define([
  './list-item-view',
  './templates'
], function (ListItemView) {

  return Marionette.CompositeView.extend({
    template: Templates['groups-list'],
    childView: ListItemView,
    childViewContainer: 'tbody'
  });

});
