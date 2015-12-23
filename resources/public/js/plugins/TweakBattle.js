//=============================================================================
// TweakBattle.js
//=============================================================================

/*:ja
 * @plugindesc 戦闘システム緊急調整
 * @author @ayamada
 *
 * @help このプラグインには、プラグインコマンドはありません。
 */

(function() {
  BattleManager.startInput = function() {
    this._phase = 'input';
    $gameParty.makeActions();
    $gameTroop.makeActions();
    this.clearActor();
    if (this._surprise) {
      this.startTurn();
    }
  };
  BattleManager.processEscape = function() {
    $gameParty.removeBattleStates();
    $gameParty.performEscape();
    SoundManager.playEscape();
    //var success = this._preemptive ? true : (Math.random() < this._escapeRatio);
    var success = true;
    if (success) {
      this.displayEscapeSuccessMessage();
      this._escaped = true;
      this.processAbort();
    } else {
      this.displayEscapeFailureMessage();
      this._escapeRatio += 0.1;
      $gameParty.clearActions();
      this.startTurn();
    }
    return success;
  };
})();
