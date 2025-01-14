/*
 * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.test.database.auto;

import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ODocumentComparator;
import com.orientechnologies.orient.core.sql.executor.OResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test(groups = {"crud", "record-document"})
public class CRUDDocumentValidationTest extends DocumentDBBaseTest {
  private ODocument record;
  private ODocument account;

  @Parameters(value = "url")
  public CRUDDocumentValidationTest(@Optional String url) {
    super(url);
  }

  @Test
  public void openDb() {
    createBasicTestSchema();

    record = database.newInstance("Whiz");
    account = new ODocument("Account");
    account.field("id", "1234567890");
  }

  @Test(dependsOnMethods = "openDb", expectedExceptions = OValidationException.class)
  public void validationMandatory() {
    record.clear();
    record.save();
  }

  @Test(dependsOnMethods = "validationMandatory", expectedExceptions = OValidationException.class)
  public void validationMinString() {
    record.clear();
    record.field("account", account);
    record.field("id", 23723);
    record.field("text", "");
    record.save();
  }

  @Test(
      dependsOnMethods = "validationMinString",
      expectedExceptions = OValidationException.class,
      expectedExceptionsMessageRegExp = "(?s).*more.*than.*")
  public void validationMaxString() {
    record.clear();
    record.field("account", account);
    record.field("id", 23723);
    record.field(
        "text",
        "clfdkkjsd hfsdkjhf fjdkghjkfdhgjdfh gfdgjfdkhgfd skdjaksdjf skdjf sdkjfsd jfkldjfkjsdf kljdk fsdjf kldjgjdhjg khfdjgk hfjdg hjdfhgjkfhdgj kfhdjghrjg");
    record.save();
  }

  @Test(
      dependsOnMethods = "validationMaxString",
      expectedExceptions = OValidationException.class,
      expectedExceptionsMessageRegExp = "(?s).*precedes.*")
  public void validationMinDate() throws ParseException {
    record.clear();
    record.field("account", account);
    record.field("date", new SimpleDateFormat("dd/MM/yyyy").parse("01/33/1976"));
    record.field("text", "test");
    record.save();
  }

  @Test(dependsOnMethods = "validationMinDate", expectedExceptions = OValidationException.class)
  public void validationEmbeddedType() throws ParseException {
    record.clear();
    record.field("account", database.getUser());
    record.save();
  }

  @Test(
      dependsOnMethods = "validationEmbeddedType",
      expectedExceptions = OValidationException.class)
  public void validationStrictClass() throws ParseException {
    ODocument doc = new ODocument("StrictTest");
    doc.field("id", 122112);
    doc.field("antani", "122112");
    doc.save();
  }

  @Test(dependsOnMethods = "validationStrictClass")
  public void closeDb() {
    database.close();
  }

  @Test(dependsOnMethods = "closeDb")
  public void createSchemaForMandatoryNullableTest() throws ParseException {
    if (database.getMetadata().getSchema().existsClass("MyTestClass")) {
      database.getMetadata().getSchema().dropClass("MyTestClass");
      database.getMetadata().getSchema().reload();
    }

    database.command("CREATE CLASS MyTestClass").close();
    database.command("CREATE PROPERTY MyTestClass.keyField STRING").close();
    database.command("ALTER PROPERTY MyTestClass.keyField MANDATORY true").close();
    database.command("ALTER PROPERTY MyTestClass.keyField NOTNULL true").close();
    database.command("CREATE PROPERTY MyTestClass.dateTimeField DATETIME").close();
    database.command("ALTER PROPERTY MyTestClass.dateTimeField MANDATORY true").close();
    database.command("ALTER PROPERTY MyTestClass.dateTimeField NOTNULL false").close();
    database.command("CREATE PROPERTY MyTestClass.stringField STRING").close();
    database.command("ALTER PROPERTY MyTestClass.stringField MANDATORY true").close();
    database.command("ALTER PROPERTY MyTestClass.stringField NOTNULL false").close();
    database
        .command(
            "INSERT INTO MyTestClass (keyField,dateTimeField,stringField) VALUES (\"K1\",null,null)")
        .close();
    database.reload();
    database.getMetadata().reload();
    database.close();
    database.open("admin", "admin");
    List<OResult> result =
        database.query("SELECT FROM MyTestClass WHERE keyField = ?", "K1").stream()
            .collect(Collectors.toList());
    Assert.assertEquals(1, result.size());
    OResult doc = result.get(0);
    Assert.assertTrue(doc.hasProperty("keyField"));
    Assert.assertTrue(doc.hasProperty("dateTimeField"));
    Assert.assertTrue(doc.hasProperty("stringField"));
  }

  @Test(dependsOnMethods = "createSchemaForMandatoryNullableTest")
  public void testUpdateDocDefined() {
    List<OResult> result =
        database.query("SELECT FROM MyTestClass WHERE keyField = ?", "K1").stream()
            .collect(Collectors.toList());
    Assert.assertEquals(1, result.size());
    OElement readDoc = result.get(0).toElement();
    readDoc.setProperty("keyField", "K1N");
    readDoc.save();
  }

  @Test(dependsOnMethods = "testUpdateDocDefined")
  public void validationMandatoryNullableCloseDb() throws ParseException {
    ODocument doc = new ODocument("MyTestClass");
    doc.field("keyField", "K2");
    doc.field("dateTimeField", (Date) null);
    doc.field("stringField", (String) null);
    doc.save();

    database.close();
    database.open("admin", "admin");

    List<OResult> result =
        database.query("SELECT FROM MyTestClass WHERE keyField = ?", "K2").stream()
            .collect(Collectors.toList());
    Assert.assertEquals(1, result.size());
    OElement readDoc = result.get(0).toElement();
    readDoc.setProperty("keyField", "K2N");
    readDoc.save();
  }

  @Test(dependsOnMethods = "validationMandatoryNullableCloseDb")
  public void validationMandatoryNullableNoCloseDb() throws ParseException {
    ODocument doc = new ODocument("MyTestClass");
    doc.field("keyField", "K3");
    doc.field("dateTimeField", (Date) null);
    doc.field("stringField", (String) null);
    doc.save();

    List<OResult> result =
        database.query("SELECT FROM MyTestClass WHERE keyField = ?", "K3").stream()
            .collect(Collectors.toList());
    Assert.assertEquals(1, result.size());
    OElement readDoc = result.get(0).toElement();
    readDoc.setProperty("keyField", "K3N");
    readDoc.save();
  }

  @Test(dependsOnMethods = "validationMandatoryNullableNoCloseDb")
  public void validationDisabledAdDatabaseLevel() throws ParseException {
    database.getMetadata().reload();
    try {
      ODocument doc = new ODocument("MyTestClass");
      doc.save();
      Assert.fail();
    } catch (OValidationException e) {
    }

    database.command("ALTER DATABASE " + ODatabase.ATTRIBUTES.VALIDATION.name() + " FALSE").close();
    database.setValidationEnabled(false);
    try {

      ODocument doc = new ODocument("MyTestClass");
      doc.save();

      doc.delete();
    } finally {
      database.setValidationEnabled(true);
      database
          .command("ALTER DATABASE " + ODatabase.ATTRIBUTES.VALIDATION.name() + " TRUE")
          .close();
    }
  }

  @Test(dependsOnMethods = "validationDisabledAdDatabaseLevel")
  public void dropSchemaForMandatoryNullableTest() throws ParseException {
    database.command("DROP CLASS MyTestClass").close();
    database.getMetadata().reload();
  }

  @Test
  public void testNullComparison() {
    // given
    ODocument doc1 = new ODocument().field("testField", (Object) null);
    ODocument doc2 = new ODocument().field("testField", (Object) null);

    ODocumentComparator comparator =
        new ODocumentComparator(
            Collections.singletonList(new OPair<String, String>("testField", "asc")),
            new OBasicCommandContext());

    Assert.assertEquals(comparator.compare(doc1, doc2), 0);
  }
}
