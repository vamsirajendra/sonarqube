define(function (require) {

  var assert = require('intern/chai!assert');
  var fs = require('intern/dojo/node!fs');
  var Command = require('intern/dojo/node!leadfoot/Command');
  var pollUntil = require('intern/dojo/node!leadfoot/helpers/pollUntil');

  var DEFAULT_TIMEOUT = 30000;

  Command.prototype.checkElementCount = function (selector, count) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector, count) {
            var elements = jQuery(selector);
            return elements.size() === count ? true : null;
          }, [selector, count], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to find ' + count + ' elements by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.checkElementExist = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector) {
            var elements = jQuery(selector);
            return elements.size() > 0 ? true : null;
          }, [selector], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to find elements by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.checkElementNotExist = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector) {
            var elements = jQuery(selector);
            return elements.size() === 0 ? true : null;
          }, [selector], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to fail to find elements by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.checkElementInclude = function (selector, text) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector, text) {
            var elements = Array.prototype.slice.call(document.querySelectorAll(selector));
            var result = elements.some(function (element) {
              return element.textContent.indexOf(text) !== -1;
            });
            return result ? true : null;
          }, [selector, text], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to find elements by selector "' + selector +
                '" that include "' + text + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.checkElementNotInclude = function (selector, text) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector, text) {
            var elements = Array.prototype.slice.call(document.querySelectorAll(selector));
            var result = elements.every(function (element) {
              return element.textContent.indexOf(text) === -1;
            });
            return result ? true : null;
          }, [selector, text], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to fail to find elements by selector "' + selector +
                '" that include "' + text + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.clickElement = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector) {
            var elements = jQuery(selector);
            if (elements.size() === 0) {
              return undefined;
            }

            var result = elements.first().click();
            return true;
          }, [selector], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to click by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.mouseUp = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector) {
            var elements = jQuery(selector);
            if (elements.size() === 0) {
              return undefined;
            }

            elements.mouseup();
            return true;
          }, [selector], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to mouseUp by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.trigger = function (selector, what) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector, what) {
            var elements = jQuery(selector);
            if (elements.size() === 0) {
              return undefined;
            }

            elements.trigger(what);
            return true;
          }, [selector, what], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to trigger by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.changeElement = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector) {
            var elements = jQuery(selector);
            if (elements.size() === 0) {
              return undefined;
            }

            elements.change();
            return true;
          }, [selector], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to change elements by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.fillElement = function (selector, value) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (selector, value) {
            var elements = jQuery(selector);
            if (elements.size() === 0) {
              return undefined;
            }

            elements.val(value);
            return true;
          }, [selector, value], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to fill elements by selector "' + selector + '". Cause: ' + cause);
          });
    });
  };

  Command.prototype.submitForm = function (selector) {
    return new this.constructor(this, function () {
      return this.parent
          .execute(function (selector) {
            jQuery(selector).submit();
          }, [selector]);
    });
  };

  Command.prototype.mockFromFile = function (url, file, options) {
    var response = fs.readFileSync('src/test/json/' + file, 'utf-8');
    return new this.constructor(this, function () {
      return this.parent
          .execute(function (url, response, options) {
            var afterComplete = function() {
              if (typeof window.complete == 'undefined') {
                window.complete = 1;
              } else {
                window.complete = window.complete + 1;
              }
            }
            return jQuery.mockjax(_.extend({ url: url, responseText: response, onAfterComplete: afterComplete }, options));
          }, [url, response, options]);
    });
  };

  Command.prototype.mockFromString = function (url, response, options) {
    return new this.constructor(this, function () {
      return this.parent
          .execute(function (url, response, options) {
            var afterComplete = function() {
              if (typeof window.complete == 'undefined') {
                window.complete = 1;
              } else {
                window.complete = window.complete + 1;
              }
            }
            return jQuery.mockjax(_.extend({ url: url, responseText: response, onAfterComplete: afterComplete }, options));
          }, [url, response, options]);
    });
  };

  Command.prototype.clearMocks = function () {
    return new this.constructor(this, function () {
      return this.parent
          .execute(function () {
            jQuery.mockjax.clear();
          });
    });
  };

  Command.prototype.startApp = function (app, options) {
    return new this.constructor(this, function () {
      var initialQueriesCount = 0;
      // TEMP
      if ((app === 'users') || (app === 'update-center') || (app === 'computation') || (app === 'custom-measures')) {
        initialQueriesCount = 2;
      } else if ((app === 'coding-rules') || (app === 'quality-profiles') || (app === 'source-viewer') || (app === 'global-permissions') || (app === 'project-permissions')) {
        initialQueriesCount = 3;
      } else if (app === 'issues') {
        initialQueriesCount = 4;
      }

      return this.parent
          .execute(function (app, options) {
            require(['apps/' + app + '/app'], function (App) {
              App.start(_.extend({ el: '#content' }, options));
            });
          }, [app, options])
          .waitForCompletion(initialQueriesCount);
    });
  };

  Command.prototype.waitForCompletion = function (count) {
    return new this.constructor(this, function () {
      return this.parent
          .then(pollUntil(function (count) {
            if (window.complete >= count) {
              return true;
            }
            return null;
          }, [count], DEFAULT_TIMEOUT))
          .then(function () {

          }, function (cause) {
            assert.fail(null, null, 'failed to wait for completion of ' + count + ' queries. Cause: ' + cause);
          });
    });
  };

  Command.prototype.open = function (hash) {
    var url = 'test/medium/base.html?' + Date.now();
    if (hash) {
      url += hash;
    }
    return new this.constructor(this, function () {
      return this.parent
          .get(require.toUrl(url))
          .mockFromString('/api/l10n/index', '{}')
          .checkElementExist('#content');
    });
  };

  Command.prototype.forceJSON = function () {
    return new this.constructor(this, function () {
      return this.parent
          .execute(function () {
            jQuery.ajaxSetup({ dataType: 'json' });
          });
    });
  };

});
