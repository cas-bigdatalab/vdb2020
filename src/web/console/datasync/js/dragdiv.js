$(function(){
    var clickX, leftOffset, inx, nextW2, nextW;
    var dragging  = false;
    var doc 	  = document;
    var labBtn 	  = $("#wrap").find('label');
    var wrapWidth = $("#wrap").width();

    labBtn.bind('mousedown',function(){
            dragging   = true;
            leftOffset = $("#wrap").offset().left;
            inx 	   = $(this).index('label');
        }
    );

    doc.onmousemove = function(e){
        if (dragging) {
            labBtn.eq(inx).prev().text( );
            labBtn.eq(inx).next().text( );
            //--------------------------------------------
            clickX = e.pageX;

            //判断第几个拖动按钮
            if( inx == 0 ){

                //第一个拖动按钮左边不出界
                if(clickX > leftOffset) {
                    labBtn.eq(inx).css('left', clickX - 7 - leftOffset + 'px');//按钮移动

                    labBtn.eq(inx).prev().width( clickX-leftOffset + 'px');
                    nextW2 = clickX-leftOffset;
                    labBtn.eq(inx).next().width( wrapWidth - nextW2 - labBtn.eq(inx+1).next().width() + 'px');
                } else {
                    labBtn.eq(inx).css('left', '0px');
                }


                // if(clickX > (labBtn.eq(inx+1).offset().left-5)) {
                //     //第一个按钮右边不出界
                //     labBtn.eq(inx).css('left', parseFloat(labBtn.eq(inx+1).css('left')) -11 + 'px');
                //     //第一个按钮，左右容器不出界
                //     labBtn.eq(inx).prev().width( labBtn.eq(inx).offset().left + 6 - leftOffset + 11 + 'px' );
                //     labBtn.eq(inx).next().width( '0px' );
                // }

            } else {

                //第二个拖动按钮左边不出界
                if( (clickX) > labBtn.eq(inx-1).offset().left + 10 ) {
                    labBtn.eq(inx).css('left', clickX - 7 - leftOffset + 'px'); //按钮移动

                    labBtn.eq(inx).prev().width( (clickX-leftOffset-labBtn.eq(inx-1).prev().width()) + 'px');
                    nextW = clickX - leftOffset;
                    labBtn.eq(inx).next().width( wrapWidth - nextW  + 'px');
                } else {
                    //第二个按钮向左移动不出界
                    labBtn.eq(inx).css('left', parseFloat(labBtn.eq(inx-1).css('left')) +10 + 'px');

                    //第二个按钮左右容器，不出界
                    labBtn.eq(inx).prev().width('0px')
                    labBtn.eq(inx).next().width( wrapWidth - labBtn.eq(inx-1).prev().width() );
                }

                if( clickX >= (leftOffset + wrapWidth) ) {
                    //第二个按钮右边不出界
                    labBtn.eq(inx).css('left', wrapWidth -10 + 'px'); //减去按钮的宽度
                    //第二个按钮左右容器，右边不出界
                    labBtn.eq(inx).prev().width( wrapWidth - labBtn.eq(inx-1).prev().width());
                    labBtn.eq(inx).next().width( '0px' );
                }

            }
        }
    };

    $(doc).mouseup(function(e) {
        dragging = false;
        e.cancelBubble = true;
    })
})