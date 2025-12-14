# AutoExtractMedicalsV2

**Version 2 – Jul 29, 2025**  
Author: Jason He

---
 
## Overview

AutoExtract Medical Services is a professional-grade Java desktop application designed to extract structured medical information from `.pdf` and `.txt` documents. With a clean and user friendly interface, the tool parses medical records into organized data fields, allowing users to easily view, filter, and export information for downstream use. Proud to be coded in Java-only.

---

## Features

- **File Loader**: Import `.pdf` or `.txt` files via file chooser.
- **Automatic Parsing**: Extracts structured fields such as patient info, vitals, diagnosis, medications, and more using regular expressions.
- **Search Bar**: Real-time filtering across field names and values.
- **Data Table**: Intuitive table view with options to hide/unhide rows.
- **Export CSV**: Save structured results to a CSV file.
- **Summary View**: Display a textual summary of extracted data.

---

## Requirements

- **Java 8+**
- **Apache PDFBox 2.x**  
  Include PDFBox in your classpath or as a Maven/Gradle dependency.

---

## Running the App

1. **Compile:**

```bash
javac -cp .:pdfbox-app-2.x.x.jar com/yourcompany/autoextract/AutoExtract.java
```

2. **Run:**
```bash
java -cp .:pdfbox-app-2.x.x.jar com.yourcompany.autoextract.AutoExtract
```

---

## License:
None.

Program by Jason He.

