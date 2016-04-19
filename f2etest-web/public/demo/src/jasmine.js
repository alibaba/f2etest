describe("suite 1", function() {
  var a;

  describe("suite 2", function() {

      it("it 1", function() {
        a = true;

        expect(a).toBe(true);
        expect(true).toBe(true);
      });

      it("it 2", function() {
        a = false;

        expect(a).toBe('2');
        expect('a').toBe('b');
      });

      it("it 3", function() {
        pending('this is why it is pending');
      });

  });

});