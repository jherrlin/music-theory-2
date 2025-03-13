export function colorElements(classId, removeFromEls, addToEls) {
    var i;
	 var j;
	 for (i = 0; i < removeFromEls.length; i++) {
		  for (j = 0; j < removeFromEls[i].length; j++) {
				removeFromEls[i][j].classList.remove(classId);
		  }
	 }
	 for (i = 0; i < addToEls.length; i++) {
		  for (j = 0; j < addToEls[i].length; j++) {
				addToEls[i][j].classList.add(classId);
		  }
	 }
}
