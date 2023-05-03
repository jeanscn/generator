package mbg.test.mb3.dsql.kotlin.miscellaneous

import mbg.test.mb3.generated.dsql.kotlin.miscellaneous.mapper.*
import mbg.test.mb3.generated.dsql.kotlin.miscellaneous.model.Generatedalwaystestnoupdates
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

class GeneratedAlwaysNoUpdatesTest : AbstractAnnotatedMiscellaneousTest() {

    @Test
    fun testInsert() {
        openSession().use { sqlSession ->
            val mapper = sqlSession.getMapper(GeneratedalwaystestnoupdatesMapper::class.java)

            val gaTest = Generatedalwaystestnoupdates()
            gaTest.id = 1
            gaTest.idPlus1 = 55
            gaTest.idPlus2 = 66
            val rows = mapper.insert(gaTest)
            assertEquals(1, rows)

            val returnedRecords = mapper.select { allRows() }
            assertEquals(1, returnedRecords.size)

            val returnedRecord = returnedRecords[0]
            assertEquals(1, returnedRecord.id)
            assertEquals(2, returnedRecord.idPlus1)
            assertEquals(3, returnedRecord.idPlus2)
        }
    }

    @Test
    fun testInsertSelective() {
        openSession().use { sqlSession ->
            val mapper = sqlSession.getMapper(GeneratedalwaystestnoupdatesMapper::class.java)

            val gaTest = Generatedalwaystestnoupdates()
            gaTest.id = 1
            val rows = mapper.insertSelective(gaTest)
            assertEquals(1, rows)

            val returnedRecords = mapper.select { allRows() }
            assertEquals(1, returnedRecords.size)

            val returnedRecord = returnedRecords[0]
            assertEquals(1, returnedRecord.id)
            assertEquals(2, returnedRecord.idPlus1)
            assertEquals(3, returnedRecord.idPlus2)
        }
    }

    @Test
    fun testThatUpdatesByPrimaryKeyDidNotGetGenerated() {
        val methods = GeneratedalwaystestnoupdatesMapper::class.java.methods
        for (method in methods) {
            if (method.name.startsWith("updateByPrimaryKey")) {
                fail<Any>("Method " + method.name + " should not be generated")
            }
        }
    }
}
