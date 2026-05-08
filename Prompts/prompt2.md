You are an Android Java developer. Build the complete UI layer for GramaKhata — all XML layout files, drawable resources, color/style/theme definitions. No Kotlin. No Compose.

=== PART A: res/values/ ===

1. colors.xml
   Define:
   - colorPrimary: #2D6A4F (deep forest green)
   - colorPrimaryVariant: #52B788
   - colorSecondary: #F4A261 (warm amber)
   - colorBackground: #FAFAF7 (off-white)
   - colorSurface: #FFFFFF
   - colorError: #E63946
   - colorCreditGreen: #40916C
   - colorDebitRed: #E63946
   - colorTextPrimary: #1B1B1B
   - colorTextSecondary: #6B7280
   - colorCardBackground: #FFFFFF
   - colorDivider: #E5E7EB

2. strings.xml
   All app strings: app_name, total_outstanding, add_customer, edit_customer, no_customers_yet,
   daily_report, send_whatsapp, delete_customer, save, cancel, credit, payment,
   customer_name_hint, phone_hint, amount_hint, note_hint, namaskara_message template,
   shop_name_dialog_title, shop_name_dialog_hint, risk_low, risk_medium, risk_high, ai_reminder

3. themes.xml
   - Theme.GramaKhata extending Theme.MaterialComponents.Light.NoActionBar
   - Apply colorPrimary, colorSecondary, colorBackground, colorSurface
   - windowBackground = colorBackground
   - Custom TextAppearance styles:
     * TextAppearance.GramaKhata.Display (size 28sp, bold, colorTextPrimary)
     * TextAppearance.GramaKhata.Title (size 20sp, semibold)
     * TextAppearance.GramaKhata.Body (size 14sp, regular)
     * TextAppearance.GramaKhata.Amount (size 22sp, bold, monospace font)
     * TextAppearance.GramaKhata.Label (size 12sp, colorTextSecondary)

4. dimens.xml
   - margin_small: 8dp, margin_medium: 16dp, margin_large: 24dp
   - card_radius: 16dp, button_height: 56dp, avatar_size: 48dp, avatar_size_large: 80dp

=== PART B: res/drawable/ ===

Create these XML drawables:
1. bg_card.xml — white background, 16dp corners, 2dp elevation shadow
2. bg_circle.xml — oval shape, colorPrimary fill (for avatar fallback)
3. bg_chip_credit.xml — rounded rect (#D1FAE5 fill), 20dp corners
4. bg_chip_payment.xml — rounded rect (#FEF3C7 fill), 20dp corners
5. bg_button_primary.xml — selector: pressed = colorPrimaryVariant, normal = colorPrimary, 12dp corners
6. bg_button_danger.xml — selector: colorError, 12dp corners
7. bg_fab.xml — oval, colorPrimary fill
8. ic_empty_ledger.xml — a simple vector drawable of an open book/ledger (create a meaningful vector path)
9. ic_whatsapp.xml — simple WhatsApp-style speech bubble vector in green
10. bg_amount_positive.xml — left border accent in colorCreditGreen (#40916C), 4dp border
11. bg_amount_negative.xml — left border accent in colorDebitRed, 4dp border

=== PART C: Layout Files ===

Create all layout XML files:

1. activity_main.xml
   - FragmentContainerView filling the screen (id: nav_host_fragment)

2. fragment_customer_list.xml
   - CoordinatorLayout root
   - AppBarLayout with Toolbar (id: toolbar, title "Grama-Khata")
   - Below toolbar: TextInputLayout + TextInputEditText for search (id: et_search)
   - LinearLayout header card (id: ll_header) showing "Total Outstanding" label and tv_total_balance TextView
   - RecyclerView (id: rv_customers) with vertical LinearLayoutManager
   - FloatingActionButton (id: fab_add, bottom-right, colorPrimary)
   - LinearLayout (id: ll_empty_state, visibility=gone, centered) with:
     - ImageView showing ic_empty_ledger
     - TextView: "No customers yet.\nTap + to add your first customer."

3. item_customer.xml
   - MaterialCardView with 16dp corners, 4dp elevation
   - Horizontal LinearLayout inside:
     - ImageView for avatar (id: iv_avatar, 48dp circle, start)
     - Vertical LinearLayout (weight=1): tv_customer_name (Title style), tv_phone (Label style)
     - Vertical LinearLayout (end): tv_net_balance (Amount style), tv_due_badge (small chip, amber)

4. fragment_customer_detail.xml
   - CoordinatorLayout
   - AppBarLayout + CollapsingToolbarLayout with Toolbar
   - NestedScrollView content:
     - Header card: ImageView iv_avatar_large (80dp), tv_customer_name (Display), tv_phone, btn_call (icon button)
     - Balance card: tv_total_due label, tv_balance_amount (large Amount style, colored)
     - Action row: btn_add_credit (green, full half-width, "＋ Credit"), btn_add_payment (amber, "－ Payment")
     - "Transaction History" section label
     - RecyclerView rv_transactions (nested scroll disabled)
     - tv_risk_score (small badge: "Risk: Low/Medium/High")
     - btn_ai_reminder (outlined button: "🤖 AI Reminder")

5. item_transaction.xml
   - MaterialCardView, 8dp corners, 2dp elevation
   - Horizontal LinearLayout:
     - TextView chip (id: tv_type_chip): "Credit" / "Payment" with bg_chip_credit or bg_chip_payment
     - Vertical LinearLayout (weight=1): tv_note, tv_timestamp (Label style)
     - TextView tv_amount (Amount style)
     - ImageButton ib_delete (trash icon, danger red tint)

6. fragment_add_edit_customer.xml
   - NestedScrollView root
   - LinearLayout (vertical, margin 16dp):
     - FrameLayout: ImageView iv_photo (80dp circle), TextView "Tap to change photo" overlay
     - TextInputLayout + TextInputEditText: Customer Name (id: et_name)
     - TextInputLayout + TextInputEditText: Phone Number (id: et_phone, inputType phone)
     - Button: "Save Customer" (id: btn_save, full width, primary style, 56dp height)
     - Button: "Delete Customer" (id: btn_delete, visibility=gone, danger text style)

7. dialog_add_transaction.xml (used as BottomSheetDialog)
   - LinearLayout vertical, 24dp padding:
     - TextView title: "Add Credit" / "Add Payment"
     - MaterialButtonToggleGroup: btn_credit, btn_payment
     - TextInputLayout (prefix ₹) + TextInputEditText: Amount (id: et_amount, inputType numberDecimal)
     - TextInputLayout + TextInputEditText: Note optional (id: et_note)
     - Button: "Save Transaction" (id: btn_save_transaction, primary, full width)

8. dialog_shop_name.xml
   - AlertDialog layout:
     - TextView title: "Welcome to Grama-Khata!"
     - TextView subtitle: "What is your shop name?"
     - TextInputLayout + TextInputEditText (id: et_shop_name)

9. dialog_daily_report.xml (BottomSheetDialog)
   - LinearLayout:
     - TextView: "Daily Report" (title)
     - ScrollView containing TextView tv_report_content (monospace font, 12sp)
     - Button: "Share Report" (id: btn_share_report)

10. dialog_ai_reminder.xml
    - AlertDialog layout:
      - TextView: "AI Generated Reminder" (title)
      - ProgressBar (id: pb_loading, visible initially)
      - TextView (id: tv_ai_message, gone initially)
      - Button: "Send via WhatsApp" (id: btn_send_whatsapp)

Output every XML file completely. No placeholders.