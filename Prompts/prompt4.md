You are an Android Java developer. Build all Fragment classes and wire the complete navigation for GramaKhata. Use Java, XML layouts, FragmentManager + Navigation Component. No Kotlin.

=== PART A: Navigation Setup ===

1. Create res/navigation/nav_graph.xml with:
   - startDestination: customerListFragment
   - fragment id="customerListFragment" (class: CustomerListFragment)
     action: action_to_detail with argument customerId (integer, defaultValue -1)
     action: action_to_add_customer
   - fragment id="customerDetailFragment" (class: CustomerDetailFragment)
     argument: customerId (integer)
     action: action_to_edit_customer with argument customerId
   - fragment id="addEditCustomerFragment" (class: AddEditCustomerFragment)
     argument: customerId (integer, defaultValue -1, meaning new customer)

2. Update activity_main.xml to use NavHostFragment with nav_graph.

3. MainActivity.java:
   - extends AppCompatActivity
   - Sets up NavController from the NavHostFragment
   - On first launch (PreferencesManager.isFirstLaunch): shows a simple AlertDialog using dialog_shop_name.xml layout, saves shop name via PreferencesManager.saveShopName(), then marks first launch done
   - Handles Up navigation via navController.navigateUp()

=== PART B: CustomerListFragment.java ===
in com.gramaKhata.ui.fragments

- extends Fragment, uses FragmentCustomerListBinding (ViewBinding)
- Creates CustomerListViewModel via new ViewModelProvider(this).get(CustomerListViewModel.class)
- Sets up RecyclerView with CustomerAdapter (LinearLayoutManager, vertical)
- Observes getFilteredCustomers() → adapter.updateList()
- Observes getTotalOutstanding() → updates tv_total_balance with CurrencyFormatter.format()
- Search: et_search addTextChangedListener → viewModel.setSearchQuery()
- FAB click → NavController navigates to addEditCustomerFragment (customerId = -1)
- CustomerAdapter.OnCustomerClickListener → navigate to customerDetailFragment with customer.id
- CustomerAdapter.OnCustomerLongClickListener → show MaterialAlertDialog "Delete [name]?" 
  Confirm → viewModel.deleteCustomer(entity) after creating CustomerEntity from CustomerWithBalance
- Empty state: observe list size → show/hide ll_empty_state vs rv_customers

=== PART C: CustomerDetailFragment.java ===
in com.gramaKhata.ui.fragments

- extends Fragment, uses FragmentCustomerDetailBinding
- Gets customerId from nav args (Bundle getArguments())
- Creates CustomerDetailViewModel, calls viewModel.setCustomerId(customerId)
- Observes customer → populate tv_customer_name, tv_phone, iv_avatar_large (Glide or initial)
- Observes netBalance → tv_balance_amount with CurrencyFormatter.format(), color CreditGreen if <=0 (no debt) else DebitRed
- Observes transactions → TransactionAdapter.updateList()
- btn_add_credit click: show AddTransactionBottomSheet with type = "CREDIT"
- btn_add_payment click: show AddTransactionBottomSheet with type = "PAYMENT"
- btn_call click: startActivity(Intent(ACTION_DIAL, Uri.parse("tel:" + phone)))
- Toolbar menu:
  - "WhatsApp" item: get shopName from PreferencesManager, call viewModel.generateWhatsAppMessage(shopName), 
    launch Intent(ACTION_SEND) with type "text/plain", EXTRA_TEXT = message, setPackage("com.whatsapp")
    Wrap in try/catch — if WhatsApp not installed, remove setPackage() and share normally
  - "Daily Report" item: show DailyReportBottomSheet
  - "Edit" item: navigate to addEditCustomerFragment with customerId

AddTransactionBottomSheet (inner static class or separate file):
- extends BottomSheetDialogFragment
- Inflates dialog_add_transaction.xml
- MaterialButtonToggleGroup toggles between CREDIT and PAYMENT
- Validates et_amount not empty and > 0
- On save: calls listener.onTransactionSaved(amount, type, note)
- Define interface OnTransactionSavedListener, set via setOnTransactionSavedListener()

DailyReportBottomSheet (separate file):
- extends BottomSheetDialogFragment
- Takes report String as argument via newInstance(String report) pattern
- Inflates dialog_daily_report.xml, sets tv_report_content = report
- btn_share_report: Intent(ACTION_SEND), type "text/plain", EXTRA_TEXT = report

GenAI features in CustomerDetailFragment:
- tv_risk_score: on fragment start, call geminiService.analyzeCustomerRisk(transactions, callback)
  Show "Analyzing..." initially, on success parse first word (Low/Medium/High) and set colored text
  Colors: Low=#40916C, Medium=#F4A261, High=#E63946
- btn_ai_reminder click: show dialog_ai_reminder.xml in AlertDialog
  Show pb_loading, call geminiService.suggestReminderMessage(..., callback)
  On success: hide pb_loading, show tv_ai_message with result
  btn_send_whatsapp: share the AI message via WhatsApp Intent

=== PART D: AddEditCustomerFragment.java ===
in com.gramaKhata.ui.fragments

- extends Fragment, uses FragmentAddEditCustomerBinding
- Gets customerId from nav args (default -1 = new customer)
- Creates AddEditCustomerViewModel
- If customerId != -1: toolbar title "Edit Customer", calls viewModel.loadCustomer(customerId), btn_delete visible
- Observes name/phone/photoUri LiveData → populate fields (only on initial load, avoid loop)
- Photo picker: ActivityResultLauncher<String> using GetContent contract for "image/*"
  On result: viewModel.setPhotoUri(uri.toString()), load with Glide into iv_photo
- btn_save click: 
  viewModel.name.setValue(et_name.getText().toString())
  viewModel.phone.setValue(et_phone.getText().toString())
  viewModel.saveCustomer(customerId)
- Observe saveSuccess: if true → navController.popBackStack()
- Observe validationError: if not null → show Snackbar with error message
- btn_delete click (only if editing): 
  Show MaterialAlertDialog "Delete this customer? All transactions will be lost."
  On confirm → viewModel.deleteCustomer(customerId) → popBackStack()
  Add deleteCustomer(int id) method to ViewModel that fetches entity then calls repository.deleteCustomer()

=== PART E: Final AndroidManifest.xml ===

Output the complete AndroidManifest.xml with:
- GramaKhataApplication as android:name
- MainActivity with:
  - intent-filter MAIN + LAUNCHER
  - screenOrientation="portrait"
  - windowSoftInputMode="adjustResize"
- All permissions: READ_EXTERNAL_STORAGE (maxSdkVersion 32), READ_MEDIA_IMAGES (minSdkVersion 33), INTERNET, CALL_PHONE
- FileProvider declaration for photo URI:
  <provider android:name="androidx.core.content.FileProvider"
    android:authorities="com.gramaKhata.fileprovider"
    android:exported="false" android:grantUriPermissions="true">
    <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths"/>
  </provider>
- Create res/xml/file_paths.xml with <external-files-path name="images" path="Pictures/"/>

=== PART F: build.gradle (final check) ===

Output the final app-level build.gradle with:
- All dependencies from Prompt 1
- buildConfigField for GEMINI_API_KEY:
  Add to local.properties: GEMINI_API_KEY=your_key_here
  Read in build.gradle:
  def localProperties = new Properties()
  localProperties.load(new FileInputStream(rootProject.file("local.properties")))
  buildConfigField "String", "GEMINI_API_KEY", "\"${localProperties['GEMINI_API_KEY']}\""
- In GeminiService constructor call: BuildConfig.GEMINI_API_KEY

Output every file completely. No partial code. No "// rest of code here" placeholders.