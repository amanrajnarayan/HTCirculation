package dev.amanraj.htcirculation.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import dev.amanraj.htcirculation.R;

public class NewAgentActivity extends AppCompatActivity {

    private Spinner spDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_agent);

        spDistrict = findViewById(R.id.spDistrict);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.district_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(adapter);

        findViewById(R.id.btnSaveAgent).setOnClickListener(v -> {
            String district = spDistrict.getSelectedItem().toString();
            generateNextAgentCode(district);
        });
    }

    //agent code creation

    private void generateNextAgentCode(String district) {
        String prefix = getDistrictPrefix(district);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("agents")
                .whereGreaterThanOrEqualTo("agentCode", prefix)
                .whereLessThan("agentCode", prefix + "Z")
                .orderBy("agentCode", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int nextNumber = 1;

                    if (!querySnapshot.isEmpty()) {
                        String lastCode = querySnapshot.getDocuments()
                                .get(0)
                                .getString("agentCode");

                        if (lastCode != null && lastCode.startsWith(prefix)) {
                            nextNumber = Integer.parseInt(lastCode.substring(3)) + 1;
                        }
                    }

                    String newAgentCode =
                            prefix + String.format("%03d", nextNumber);

                    Log.d("AGENT_CODE", "Generated: " + newAgentCode);
                    saveAgent(newAgentCode);
                });
    }

    //save agent

    private void saveAgent(String agentCode) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String name = ((EditText) findViewById(R.id.etAgentName))
                .getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Agent name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = ((EditText) findViewById(R.id.etPassword))
                .getText().toString().trim();

        if (password.length() < 6) {
            Toast.makeText(
                    this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String unsoldStr = ((EditText) findViewById(R.id.etUnsoldLimit))
                .getText().toString().trim();

        String depositStr = ((EditText) findViewById(R.id.etSecurityDeposit))
                .getText().toString().trim();

        int unsoldLimit = unsoldStr.isEmpty() ? 0 : Integer.parseInt(unsoldStr);
        int securityDeposit = depositStr.isEmpty() ? 0 : Integer.parseInt(depositStr);

        Map<String, Object> agent = new HashMap<>();
        agent.put("agentCode", agentCode);
        agent.put("name", name);
        agent.put("district", spDistrict.getSelectedItem().toString());
        agent.put("address",
                ((EditText) findViewById(R.id.etAddress)).getText().toString().trim());
        agent.put("dropPoint",
                ((EditText) findViewById(R.id.etDropPoint)).getText().toString().trim());
        agent.put("locationUrl",
                ((EditText) findViewById(R.id.etLocationUrl)).getText().toString().trim());
        agent.put("unsoldLimit", unsoldLimit);
        agent.put("securityDeposit", securityDeposit);
        agent.put("active", true);
        agent.put("createdAt", FieldValue.serverTimestamp());

        db.collection("agents")
                .document(agentCode)
                .set(agent)
                .addOnSuccessListener(unused ->
                        createAgentLogin(agentCode, name, password)
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    //agent login creation

    private void createAgentLogin(
            String agentCode,
            String name,
            String password
    ) {

        String email = agentCode.toLowerCase() + "@poms.ht";

        FirebaseOptions options = FirebaseApp.getInstance().getOptions();
        FirebaseApp secondaryApp;

        try {
            secondaryApp =
                    FirebaseApp.initializeApp(this, options, "SecondaryAuth");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.getInstance("SecondaryAuth");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("role", "agent");
                    user.put("agentCode", agentCode);
                    user.put("name", name);
                    user.put("district", spDistrict.getSelectedItem().toString());
                    user.put("email", email);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(
                                        this,
                                        "Agent created. Login ID: " + email,
                                        Toast.LENGTH_LONG
                                ).show();
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    //district prefix

    private String getDistrictPrefix(String district) {
        switch (district) {
            case "Patna": return "PAT";
            case "Gaya": return "GAY";
            case "Muzaffarpur": return "MUZ";
            case "Bhagalpur": return "BGP";
            case "Darbhanga": return "DBG";
            case "Purnia": return "PUN";
            case "Araria": return "ARA";
            case "Arwal": return "ARW";
            case "Aurangabad": return "AUR";
            case "Banka": return "BNK";
            case "Begusarai": return "BEG";
            case "Bhojpur": return "BHO";
            case "Buxar": return "BUX";
            case "East Champaran": return "ECH";
            case "West Champaran": return "WCH";
            case "Gopalganj": return "GOP";
            case "Jamui": return "JAM";
            case "Jehanabad": return "JEH";
            case "Kaimur": return "KAI";
            case "Katihar": return "KAT";
            case "Khagaria": return "KHG";
            case "Kishanganj": return "KIS";
            case "Lakhisarai": return "LAK";
            case "Madhepura": return "MAD";
            case "Madhubani": return "MDB";
            case "Munger": return "MUN";
            case "Nalanda": return "NAL";
            case "Nawada": return "NAW";
            case "Rohtas": return "ROT";
            case "Saharsa": return "SAH";
            case "Samastipur": return "SAM";
            case "Saran": return "SAR";
            case "Sheikhpura": return "SHE";
            case "Sheohar": return "SHR";
            case "Sitamarhi": return "SIT";
            case "Siwan": return "SIW";
            case "Supaul": return "SUP";
            case "Vaishali": return "VAI";
            default: return "UNK";
        }
    }
}
