QUnit.module( "group a" );
QUnit.test( "a basic test example", function( assert ) {
  assert.ok( true, "this test is fine" );
  assert.ok( false, "false111" );
  assert.ok( false, "false222" );
});
QUnit.test( "a basic test example 2", function( assert ) {
  assert.ok( true, "this test is fine" );
});
 
QUnit.module( "group b" );
QUnit.test( "a basic test example 3", function( assert ) {
  assert.ok( true, "this test is fine" );
});