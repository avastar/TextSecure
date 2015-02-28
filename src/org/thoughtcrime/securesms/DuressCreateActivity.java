/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.thoughtcrime.securesms.crypto.InvalidPassphraseException;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.MasterSecretUtil;
import org.thoughtcrime.securesms.util.MemoryCleaner;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

/**
 * Activity for changing a user's local encryption passphrase.
 *
 * @author Moxie Marlinspike
 */

public class DuressCreateActivity extends PassphraseActivity {

  private EditText passphrase;
  private EditText duress;
  private EditText repeatDuress;
  private Button   okButton;
  private Button   cancelButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.create_duress_activity);

    initializeResources();
  }

  private void initializeResources() {

    this.passphrase              = (EditText) findViewById(R.id.passphrase      );
    this.duress                  = (EditText) findViewById(R.id.duress          );
    this.repeatDuress            = (EditText) findViewById(R.id.repeat_duress       );

    this.okButton                = (Button  ) findViewById(R.id.ok_button           );
    this.cancelButton            = (Button  ) findViewById(R.id.cancel_button       );

    this.okButton.setOnClickListener(new OkButtonClickListener());
    this.cancelButton.setOnClickListener(new CancelButtonClickListener());
  }

  private void verifyAndSavePassphrases() {
    Editable originalText = this.passphrase.getText();
    Editable newText      = this.duress.getText();
    Editable repeatText   = this.repeatDuress.getText();

    String passphrase     = (originalText == null ? "" : originalText.toString());
    String duress         = (newText == null ? "" : newText.toString());
    String repeatDuress   = (repeatText == null ? "" : repeatText.toString());

    try {
      if (!duress.equals(repeatDuress)) {
        Toast.makeText(getApplicationContext(),
              R.string.PassphraseChangeActivity_passphrases_dont_match_exclamation,
              Toast.LENGTH_SHORT).show();
        this.duress.setText("");
        this.repeatDuress.setText("");
      } else {
        MasterSecret masterSecret = MasterSecretUtil.getMasterSecret(this, passphrase);
        MasterSecretUtil.generateMasterSecretDuress(this, duress, masterSecret);

        MemoryCleaner.clean(passphrase);
        MemoryCleaner.clean(duress);
        MemoryCleaner.clean(repeatDuress);

        setMasterSecret(masterSecret);
      }
    } catch (InvalidPassphraseException e) {
        Toast.makeText(this, R.string.PassphraseChangeActivity_incorrect_old_passphrase_exclamation,
        Toast.LENGTH_LONG).show();
        this.passphrase.setText("");
    }
  }

  private class CancelButtonClickListener implements OnClickListener {
    public void onClick(View v) {
      finish();
    }
  }

  private class OkButtonClickListener implements OnClickListener {
    public void onClick(View v) {
      verifyAndSavePassphrases();
    }
  }

  @Override
  protected void cleanup() {
    this.passphrase = null;
    this.duress = null;
    this.repeatDuress = null;

    System.gc();
  }
}
