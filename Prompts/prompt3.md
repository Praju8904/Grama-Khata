You are an Android Java developer. Build all Java logic classes for GramaKhata: ViewModels, RecyclerView Adapters, and utility classes. Use LiveData, ViewModel, MVVM. No Kotlin.

=== PART A: ViewModels ===

1. CustomerListViewModel.java (extends AndroidViewModel)
   in com.gramaKhata.ui.viewmodel

   Fields:
   - GramaKhataRepository repository
   - LiveData<List<CustomerDao.CustomerWithBalance>> allCustomers (from repository)
   - MutableLiveData<String> searchQuery = new MutableLiveData<>("")

   Methods:
   - LiveData<List<CustomerDao.CustomerWithBalance>> getFilteredCustomers()
     Use MediatorLiveData that observes both allCustomers and searchQuery.
     Filter: customer.name.toLowerCase().contains(query.toLowerCase())
     Return sorted by netBalance descending.
   - void setSearchQuery(String query)
   - void deleteCustomer(CustomerEntity customer) — runs on diskIO executor
   - LiveData<Double> getTotalOutstanding()
     Use Transformations.map on allCustomers to sum all netBalance values where netBalance > 0.

2. CustomerDetailViewModel.java (extends AndroidViewModel)
   in com.gramaKhata.ui.viewmodel

   Fields:
   - GramaKhataRepository repository
   - int customerId (set via setCustomerId)
   - LiveData<CustomerEntity> customer
   - LiveData<List<TransactionEntity>> transactions
   - LiveData<Double> netBalance

   Methods:
   - void setCustomerId(int id) — initializes the above LiveData via repository
   - void addTransaction(double amount, String type, String note)
     Creates TransactionEntity with System.currentTimeMillis() as timestamp, calls repository.insertTransaction()
   - void deleteTransaction(TransactionEntity transaction) — calls repository.deleteTransaction()
   - String generateWhatsAppMessage(String shopName)
     Returns: "Namaskara, your due at [shopName] is ₹[formatted netBalance]. Please pay at your earliest convenience."
     Use String.format("%.2f", netBalance) for amount.
   - String generateDailyReport(String shopName)
     Gets current transactions LiveData value, filters those where timestamp is today (compare Calendar date).
     Builds report string:
     "--- Grama-Khata Daily Report ---\nDate: [DD/MM/YYYY]\nShop: [shopName]\n\n[lines]\n---\nTotal Credited: ₹X\nTotal Collected: ₹Y\n---"

3. AddEditCustomerViewModel.java (extends AndroidViewModel)
   in com.gramaKhata.ui.viewmodel

   Fields:
   - GramaKhataRepository repository
   - MutableLiveData<String> name, phone, photoUri
   - MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>()
   - MutableLiveData<String> validationError = new MutableLiveData<>()

   Methods:
   - void loadCustomer(int customerId) — fetches and populates name/phone/photoUri from LiveData
   - void saveCustomer(int existingId)
     Validates name and phone not blank.
     If existingId == -1: insert new CustomerEntity
     Else: update existing CustomerEntity (keep createdAt)
     On success: postValue(true) to saveSuccess
   - void setPhotoUri(String uri)

=== PART B: RecyclerView Adapters ===

1. CustomerAdapter.java in com.gramaKhata.ui.adapter
   - extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>
   - Uses ViewBinding (ItemCustomerBinding)
   - Constructor takes List<CustomerDao.CustomerWithBalance> and two listeners:
     interface OnCustomerClickListener { void onClick(CustomerDao.CustomerWithBalance customer); }
     interface OnCustomerLongClickListener { void onLongClick(CustomerDao.CustomerWithBalance customer); }
   - ViewHolder binds:
     - tv_customer_name = customer.name
     - tv_phone = customer.phone
     - tv_net_balance = formatted ₹amount, colored CreditGreen if > 0, DebitRed if < 0
     - tv_due_badge: visible only if netBalance > 0, text "Due"
     - iv_avatar: load with Glide if photoUri not null, else show initial letter in bg_circle
   - void updateList(List<CustomerDao.CustomerWithBalance> newList) — uses DiffUtil
   - Implement DiffUtil.Callback as static inner class (compare by id, detect content changes)

2. TransactionAdapter.java in com.gramaKhata.ui.adapter
   - extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>
   - Uses ViewBinding (ItemTransactionBinding)
   - Constructor takes List<TransactionEntity> and OnDeleteListener interface
   - ViewHolder binds:
     - tv_type_chip: text = type, setBackground to bg_chip_credit or bg_chip_payment
     - tv_note: transaction.note (gone if null/empty)
     - tv_timestamp: formatted with SimpleDateFormat("dd MMM yyyy, hh:mm a")
     - tv_amount: "₹" + String.format("%.2f", amount)
     - ib_delete: calls listener.onDelete(transaction)
   - void updateList(List<TransactionEntity> newList) with DiffUtil

=== PART C: Utility Classes ===

1. CurrencyFormatter.java in com.gramaKhata.utils
   - static String format(double amount) → "₹1,234.56" using NumberFormat.getCurrencyInstance with INR locale
   - static String formatAbs(double amount) → absolute value formatted

2. DateUtils.java in com.gramaKhata.utils
   - static String formatDate(long timestamp) → "dd MMM yyyy"
   - static String formatDateTime(long timestamp) → "dd MMM yyyy, hh:mm a"
   - static boolean isToday(long timestamp) — compare Calendar DAY_OF_YEAR and YEAR
   - static String todayFormatted() → today's date as "DD/MM/YYYY"

3. AvatarUtils.java in com.gramaKhata.utils
   - static int getColorForName(String name) — deterministic color from name.hashCode() picking from a curated array of 8 earthy/warm colors
   - static String getInitial(String name) — returns first character uppercase

4. PreferencesManager.java in com.gramaKhata.utils
   - Uses SharedPreferences (not DataStore, simpler for Java)
   - static void saveShopName(Context context, String shopName)
   - static String getShopName(Context context) — returns "" if not set
   - static boolean isFirstLaunch(Context context)
   - static void setFirstLaunchDone(Context context)

5. GeminiService.java in com.gramaKhata.data.ai
   - Constructor: GeminiService(String apiKey)
   - Uses GenerativeModel from Gemini SDK (generativeai)
   - Interface GeminiCallback { void onSuccess(String result); void onError(String error); }
   - void suggestReminderMessage(String customerName, String shopName, double amount, GeminiCallback callback)
     Prompt: "Write a polite, short WhatsApp reminder in Kannada and English for a village shopkeeper named [shopName] to send to their customer [customerName] who owes ₹[amount]. Keep it friendly and under 2 sentences."
     Run on background thread, callback on main thread using Handler(Looper.getMainLooper())
   - void analyzeCustomerRisk(List<TransactionEntity> transactions, GeminiCallback callback)
     Build last-10 transaction summary string, then prompt:
     "Based on this transaction history for a village grocery store customer, give a 1-sentence risk assessment: Low Risk / Medium Risk / High Risk and one sentence of reasoning."
   - void generateSmartNote(double amount, String type, GeminiCallback callback)
     Prompt: "Suggest a short 3-5 word transaction note for a [type] of ₹[amount] in a village grocery store. Return only the note, nothing else."

Output all Java files completely with full implementation.