package com.michaelbarany.perka;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final int REQUEST_CODE_RESUME = 1001;
        private static final String STATE_RESUME = "state_resume";

        private Uri mResume;
        private Button mBtnResume;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (null != mResume) {
                outState.putString(STATE_RESUME, mResume.toString());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ((TextView) rootView.findViewById(R.id.email)).setText(getEmail());
            mBtnResume = (Button) rootView.findViewById(R.id.btn_resume);
            mBtnResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("application/pdf");
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(i, REQUEST_CODE_RESUME);
                }
            });

            if (null != savedInstanceState && savedInstanceState.containsKey(STATE_RESUME)) {
                mResume = Uri.parse(savedInstanceState.getString(STATE_RESUME));
                mBtnResume.setEnabled(false);
                mBtnResume.setText("Attached");
            }
            return rootView;
        }

        private String getEmail() {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(getActivity()).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    return account.name;
                }
            }
            return null;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_RESUME && resultCode == Activity.RESULT_OK && null != data.getData()) {
                mResume = data.getData();
                mBtnResume.setEnabled(false);
                mBtnResume.setText("Attached");
            }
        }

        private byte[] getBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.action_send) {
                ApplicationForm applicationForm = new ApplicationForm();
                applicationForm.first_name = ((EditText) getView().findViewById(R.id.first_name)).getText().toString();
                applicationForm.last_name = ((EditText) getView().findViewById(R.id.last_name)).getText().toString();
                applicationForm.email = ((EditText) getView().findViewById(R.id.email)).getText().toString();
                applicationForm.source = ((EditText) getView().findViewById(R.id.source)).getText().toString();
                if (!applicationForm.validates() || null == mResume) {
                    Toast.makeText(getActivity(), "Fill out all fields!", Toast.LENGTH_LONG).show();
                    return true;
                }
                item.setEnabled(false);

                try {
                    InputStream iStream = getActivity().getContentResolver().openInputStream(mResume);
                    applicationForm.resume = Base64.encodeToString(getBytes(iStream), Base64.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                    item.setEnabled(true);
                    return true;
                }

                sendApplication(applicationForm, item);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void sendApplication(ApplicationForm applicationForm, final MenuItem item) {
            final Toast toast = Toast.makeText(getActivity(), "Sending...", Toast.LENGTH_LONG);

            ApplyService service = Api.getRestAdapter().create(ApplyService.class);
            service.index(applicationForm, new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    toast.cancel();
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    toast.cancel();
                    Toast.makeText(getActivity(), "Failure!", Toast.LENGTH_LONG).show();
                    item.setEnabled(true);
                }
            });
            toast.show();
        }
    }

}
