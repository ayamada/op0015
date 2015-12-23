//=============================================================================
// TweakTitle.js
//=============================================================================

/*:ja
 * @plugindesc タイトル画面緊急調整
 * @author @ayamada
 *
 * @help このプラグインには、プラグインコマンドはありません。
 */

(function() {
  Scene_Title.prototype.drawGameTitle = function() {
    var x = 20;
    var y = Graphics.height / 2.5;
    var maxWidth = Graphics.width - x * 2;
    var text = $dataSystem.gameTitle;
    this._gameTitleSprite.bitmap.outlineColor = 'black';
    this._gameTitleSprite.bitmap.outlineWidth = 8;
    this._gameTitleSprite.bitmap.fontSize = 72;
    this._gameTitleSprite.bitmap.drawText(text, x, y, maxWidth, 48, 'center');
  };

  Scene_Title.prototype.createCommandWindow = function() {
    this._commandWindow = new Window_TitleCommand();
    this._commandWindow.setHandler('newGame',  this.commandNewGame.bind(this));
    //this._commandWindow.setHandler('continue', this.commandContinue.bind(this));
    //this._commandWindow.setHandler('options',  this.commandOptions.bind(this));
    this._commandWindow.setHandler('options', function() {
      //window.location = "https://github.com/ayamada/op0015/blob/master/clojure-advent-calendar-2015.md";
      //window.open("https://github.com/ayamada/op0015/blob/master/clojure-advent-calendar-2015.md");
      op0015.core.open_ac();
      SceneManager.goto(Scene_Title);
    });
    this.addWindow(this._commandWindow);
  };
})();
