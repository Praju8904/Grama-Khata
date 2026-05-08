You are an Android Java developer. Build the complete foundation for "GramaKhata", a micro-finance ledger app for village shopkeepers. Use Java, XML layouts, Room DB, LiveData, and MVVM.

=== PART A: build.gradle (app level) ===

Add these dependencies:
- Room: room-runtime:2.6.1, room-compiler:2.6.1 (via annotationProcessor)
- Lifecycle: lifecycle-viewmodel:2.7.0, lifecycle-livedata:2.7.0, lifecycle-runtime:2.7.0
- Glide: glide:4.16.0, glide compiler (annotationProcessor)
- DataStore: datastore-preferences:1.0.0 (for shop name storage)
- Gemini: com.google.ai.client.generativeai:generativeai:0.7.0
- Material Components: com.google.android.material:material:1.11.0
- RecyclerView: recyclerview:1.3.2
- CardView: cardview:1.0.0

Enable:
- viewBinding = true
- Java 8 compatibility (compileOptions sourceCompatibility/targetCompatibility JavaVersion.VERSION_1_8)

=== PART B: Room Database Layer ===

Create the following in com.gramaKhata.data.db:

1. CustomerEntity.java
   @Entity(tableName = "customers")
   Fields:
   - @PrimaryKey(autoGenerate = true) int id
   - String name
   - String photoUri (nullable)
   - String phone
   - long createdAt (System.currentTimeMillis())

2. TransactionEntity.java
   @Entity(tableName = "transactions", foreignKeys = @ForeignKey referencing CustomerEntity.id)
   Fields:
   - @PrimaryKey(autoGenerate = true) int id
   - int customerId
   - double amount
   - String type ("CREDIT" or "PAYMENT")
   - String note (nullable)
   - long timestamp (System.currentTimeMillis())

3. CustomerDao.java (interface annotated with @Dao)
   Methods:
   - @Insert long insertCustomer(CustomerEntity customer)
   - @Update void updateCustomer(CustomerEntity customer)
   - @Delete void deleteCustomer(CustomerEntity customer)
   - @Query getAllCustomers(): LiveData<List<CustomerEntity>>
   - @Query getCustomerById(int id): LiveData<CustomerEntity>
   - @Query getCustomersWithNetBalance(): LiveData<List<CustomerWithBalance>>
     SQL: SELECT c.id, c.name, c.photoUri, c.phone,
          SUM(CASE WHEN t.type='CREDIT' THEN t.amount ELSE -t.amount END) as netBalance
          FROM customers c LEFT JOIN transactions t ON c.id = t.customerId
          GROUP BY c.id ORDER BY netBalance DESC
   Define CustomerWithBalance as a static inner POJO class inside CustomerDao with fields: int id, String name, String photoUri, String phone, double netBalance.

4. TransactionDao.java (interface annotated with @Dao)
   Methods:
   - @Insert long insertTransaction(TransactionEntity t)
   - @Delete void deleteTransaction(TransactionEntity t)
   - @Query getTransactionsForCustomer(int customerId): LiveData<List<TransactionEntity>>
   - @Query getNetBalance(int customerId): LiveData<Double>
     SQL: SELECT SUM(CASE WHEN type='CREDIT' THEN amount ELSE -amount END) FROM transactions WHERE customerId = :customerId

5. AppDatabase.java
   - @Database(entities = {CustomerEntity.class, TransactionEntity.class}, version = 1)
   - Abstract methods: CustomerDao customerDao(), TransactionDao transactionDao()
   - Singleton with volatile static instance and synchronized getInstance(Context) method
   - fallbackToDestructiveMigration()

=== PART C: Repository ===

Create GramaKhataRepository.java in com.gramaKhata.data.repository:
- Constructor: GramaKhataRepository(Application application)
- Internally creates AppDatabase and holds CustomerDao and TransactionDao
- Expose via LiveData wrappers (never return raw DAO calls directly):
  - LiveData<List<CustomerDao.CustomerWithBalance>> getAllCustomersWithBalance()
  - LiveData<CustomerEntity> getCustomerById(int id)
  - LiveData<List<TransactionEntity>> getTransactionsForCustomer(int customerId)
  - LiveData<Double> getNetBalance(int customerId)
- Use AppExecutors (create a simple AppExecutors.java with a single-thread diskIO executor) for all write operations:
  - void insertCustomer(CustomerEntity customer)
  - void updateCustomer(CustomerEntity customer)
  - void deleteCustomer(CustomerEntity customer)
  - void insertTransaction(TransactionEntity transaction)
  - void deleteTransaction(TransactionEntity transaction)

Create AppExecutors.java in com.gramaKhata.utils:
- Holds Executor diskIO = Executors.newSingleThreadExecutor()
- Holds Handler mainThread = new Handler(Looper.getMainLooper())
- Singleton pattern

=== PART D: Application Class ===

Create GramaKhataApplication.java extending Application in com.gramaKhata:
- Holds a static GramaKhataRepository repository instance
- static GramaKhataRepository getRepository() method
- Register in AndroidManifest.xml: android:name=".GramaKhataApplication"

Also update AndroidManifest.xml:
- READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES permissions (with maxSdkVersion for older)
- INTERNET permission
- android:screenOrientation="portrait" on MainActivity
- android:windowSoftInputMode="adjustResize" on MainActivity
- FileProvider for photo URI (authority: "com.gramaKhata.fileprovider")

Output every file completely in Java. No Kotlin. No Compose.