import _ from 'underscore';

module.exports = function (array, options) {
  var cond = _.isArray(array) && array.length > 0;
  return cond ? options.inverse(this) : options.fn(this);
};
