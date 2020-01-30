package com.seagates3.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.policy.Condition;

public
class BucketPolicyAuthorizerTest {

  static List<String> values = null;
  Map<String, String> requestBody = null;
  String key = "x-amz-acl";
  BucketPolicyAuthorizer bpAuthorizer = new BucketPolicyAuthorizer();
  static Method isConditionMatching = null;

  @Before public void setUp() {
    values = new ArrayList<>();
    requestBody = new HashMap<String, String>();
  }

  @BeforeClass public static void setUpBeforeClass()
      throws NoSuchMethodException,
      SecurityException {
    isConditionMatching = BucketPolicyAuthorizer.class.getDeclaredMethod(
        "isConditionMatching", List.class, Map.class);
    isConditionMatching.setAccessible(true);
  }

  @Test public void testAuthorizePolicy_StringEquals_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringNotEquals_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("ABC");
    Condition condition = new Condition();
    condition.setType("StringNotEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringEquals_false() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("pqr");
    Condition condition = new Condition();
    condition.setType("StringEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_StringEqualsIgnoreCase_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "Abc");
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringEqualsIgnoreCase");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringEqualsIgnoreCase_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "pqr");
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringEqualsIgnoreCase");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_StringNotEqualsIgnoreCase_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "pqr");
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringNotEqualsIgnoreCase");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringNotEqualsIgnoreCase_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abC");
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringNotEqualsIgnoreCase");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_StringLike_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("ab?");
    Condition condition = new Condition();
    condition.setType("StringLike");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringLike_false() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("a*d");
    Condition condition = new Condition();
    condition.setType("StringLike");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_StringNotLike_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "abc");
    values.add("a*d");
    Condition condition = new Condition();
    condition.setType("StringNotLike");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_StringEqualsIfExists_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    values.add("abc");
    Condition condition = new Condition();
    condition.setType("StringEqualsIfExists");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericEquals_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "10");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericNotEquals_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "11");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericNotEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericEquals_false() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "10");
    values.add("11");
    Condition condition = new Condition();
    condition.setType("NumericEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NumericGreaterThan_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "11");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericGreaterThan");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericGreaterThan_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "9");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericGreaterThan");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NumericGreaterThanEquals_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "10");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericGreaterThanEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericGreaterThanEquals_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "9");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericGreaterThanEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NumericLessThan_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "9");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericLessThan");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericLessThan_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "10");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericLessThan");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NumericLessThanEquals_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "10");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericLessThanEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_NumericLessThanEquals_false()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    requestBody.put(key, "11");
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericLessThanEquals");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NumericEqualsIfExists_true()
      throws Exception {
    requestBody = new HashMap<String, String>();
    key = "max-keys";
    values.add("10");
    Condition condition = new Condition();
    condition.setType("NumericEqualsIfExists");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_Bool_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "true");
    values.add("true");
    Condition condition = new Condition();
    condition.setType("Bool");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_Bool_false() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "false");
    values.add("true");
    Condition condition = new Condition();
    condition.setType("Bool");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_BoolIfExists_true() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    values.add("false");
    Condition condition = new Condition();
    condition.setType("BoolIfExists");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_BoolIfExists_false() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "true");
    values.add("false");
    Condition condition = new Condition();
    condition.setType("BoolIfExists");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  /*@Test
  public void testAuthorizePolicy_DateEquals_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "10");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateNotEquals_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "11");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateNotEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateEquals_false() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "10");
      values.add("11");
      Condition condition = new Condition();
      condition.setType("DateEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertFalse((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateGreaterThan_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "11");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateGreaterThan");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateGreaterThan_false() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "9");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateGreaterThan");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertFalse((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateGreaterThanEquals_true() throws Exception
  {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "10");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateGreaterThanEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateGreaterThanEquals_false()
  throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "9");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateGreaterThanEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertFalse((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateLessThan_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "9");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateLessThan");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateLessThan_false() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "10");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateLessThan");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertFalse((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateLessThanEquals_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "10");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateLessThanEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateLessThanEquals_false() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      requestBody.put(key, "11");
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateLessThanEquals");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertFalse((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }

  @Test
  public void testAuthorizePolicy_DateEqualsIfExists_true() throws Exception {
      requestBody = new HashMap<String, String>();
      key = "max-keys";
      values.add("10");
      Condition condition = new Condition();
      condition.setType("DateEqualsIfExists");
      condition.setConditionKey(key);
      condition.setValues(values);
      List<Condition> conditions = new ArrayList<>();
      conditions.add(condition);
      assertTrue((Boolean)isConditionMatching.invoke(
          bpAuthorizer, conditions, requestBody));
  }*/

  @Test public void testAuthorizePolicy_Null_true_success() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    values.add("true");
    Condition condition = new Condition();
    condition.setType("Null");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_Null_true_fail() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "true");
    values.add("true");
    Condition condition = new Condition();
    condition.setType("Null");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_Null_false_success() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "abc");
    values.add("false");
    Condition condition = new Condition();
    condition.setType("Null");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertTrue((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                   requestBody));
  }

  @Test public void testAuthorizePolicy_Null_false_fail() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, null);
    values.add("false");
    Condition condition = new Condition();
    condition.setType("Null");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }

  @Test public void testAuthorizePolicy_NullIfExists_fail() throws Exception {
    requestBody = new HashMap<String, String>();
    key = "abc-keys";
    requestBody.put(key, "abc");
    values.add("false");
    Condition condition = new Condition();
    condition.setType("NullIfExists");
    condition.setConditionKey(key);
    condition.setValues(values);
    List<Condition> conditions = new ArrayList<>();
    conditions.add(condition);
    assertFalse((Boolean)isConditionMatching.invoke(bpAuthorizer, conditions,
                                                    requestBody));
  }
}