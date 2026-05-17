package tkhug.project.pocketrocket.data.db

import androidx.room.TypeConverter
import tkhug.project.pocketrocket.data.model.AccountType
import tkhug.project.pocketrocket.data.model.PeriodType
import tkhug.project.pocketrocket.data.model.TransactionType

class Converters {

    @TypeConverter fun txTypeToString(v: TransactionType): String = v.name
    @TypeConverter fun stringToTxType(v: String): TransactionType = TransactionType.valueOf(v)

    @TypeConverter fun accountTypeToString(v: AccountType): String = v.name
    @TypeConverter fun stringToAccountType(v: String): AccountType = AccountType.valueOf(v)

    @TypeConverter fun periodTypeToString(v: PeriodType): String = v.name
    @TypeConverter fun stringToPeriodType(v: String): PeriodType = PeriodType.valueOf(v)
}

