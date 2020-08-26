package by.liauko.siarhei.cl.util

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import by.liauko.siarhei.cl.entity.LogData
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportToExcelAsyncTask(
    private val context: Context,
    private val directoryUri: Uri,
    private val data: List<LogData>
)
    : AsyncTask<Unit, Unit, Unit>() {

    private lateinit var progressDialog: ProgressDialog

    override fun onPreExecute() {
        super.onPreExecute()

        progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_export_text
        )
        progressDialog.show()
    }

    override fun doInBackground(vararg params: Unit?) {
        val workBook = XSSFWorkbook()

        val sheet = workBook.createSheet(ApplicationUtil.profileName)
        sheet.columnHelper.setColWidth(0, 12.0)
        sheet.columnHelper.setColWidth(1, 12.0)
        sheet.columnHelper.setColWidth(2, 60.0)

        val headerStyle = workBook.createCellStyle()
        val boldFont = workBook.createFont()
        boldFont.bold = true
        headerStyle.setFont(boldFont)
        headerStyle.borderBottom = BorderStyle.THICK

        val firstRow = sheet.createRow(0)
        createStringCell(0, firstRow, headerStyle, context.getString(R.string.log_spreadsheet_created_date_label))
        createStringCell(1, firstRow, headerStyle, context.getString(R.string.log_spreadsheet_mileage_label))
        createStringCell(2, firstRow, headerStyle, context.getString(R.string.log_spreadsheet_description_label))

        for ((i, entity) in data.withIndex()) {
            val date = Date(entity.time)
            val row = sheet.createRow(i + 1)

            val dateStyle = workBook.createCellStyle()
            dateStyle.verticalAlignment = VerticalAlignment.TOP
            createStringCell(0, row, dateStyle, SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date))

            val mileageStyle = workBook.createCellStyle()
            mileageStyle.alignment = HorizontalAlignment.LEFT
            mileageStyle.verticalAlignment = VerticalAlignment.TOP
            val mileageCell = row.createCell(1, CellType.NUMERIC)
            mileageCell.setCellValue(entity.mileage.toDouble())
            mileageCell.cellStyle = mileageStyle

            val textStyle = workBook.createCellStyle()
            textStyle.wrapText = true
            createStringCell(2, row, textStyle, entity.text)
        }

        val file = DocumentFile.fromTreeUri(context, directoryUri)?.createFile(
            MimeTypes.TYPE_XLSX_FILE,
            "${ApplicationUtil.profileName.replace(" ", "-")}-log-data"
        )
        if (file?.uri != null) {
            context.contentResolver.openOutputStream(file.uri)?.use {
                workBook.write(it)
            }
        }
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)

        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }

    private fun createStringCell(index: Int, row: XSSFRow, style: XSSFCellStyle, text: String) {
        val cell = row.createCell(index, CellType.STRING)
        cell.cellStyle = style
        cell.setCellValue(text)
    }
}