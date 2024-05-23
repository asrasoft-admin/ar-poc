#import <Foundation/Foundation.h>

@interface ARModule : NSObject

+ (void)startARSession;
+ (BOOL)requiresMainQueueSetup;

@end
