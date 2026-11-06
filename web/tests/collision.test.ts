import { test, describe } from 'node:test';
import assert from 'node:assert';
import { CollisionManager } from '../src/game/CollisionManager';
import { BoundingBox } from '../src/types/game';

describe('Collision Detection Mathematics Unit Tests', () => {
  test('AABB collision correctly detects overlapping bounding boxes', () => {
    const boxA: BoundingBox = { x: 100, y: 100, width: 50, height: 50 };
    const boxB: BoundingBox = { x: 120, y: 120, width: 40, height: 40 };

    assert.strictEqual(CollisionManager.checkAABB(boxA, boxB), true);
    assert.strictEqual(CollisionManager.checkAABB(boxB, boxA), true);
  });

  test('AABB collision correctly detects separated bounding boxes', () => {
    const boxA: BoundingBox = { x: 100, y: 100, width: 50, height: 50 };
    const boxFarRight: BoundingBox = { x: 200, y: 100, width: 50, height: 50 };
    const boxFarBelow: BoundingBox = { x: 100, y: 200, width: 50, height: 50 };

    assert.strictEqual(CollisionManager.checkAABB(boxA, boxFarRight), false);
    assert.strictEqual(CollisionManager.checkAABB(boxA, boxFarBelow), false);
  });

  test('AABB collision handles touching edges as non-penetrating', () => {
    const boxA: BoundingBox = { x: 100, y: 100, width: 50, height: 50 };
    const boxTouchingRight: BoundingBox = { x: 150, y: 100, width: 50, height: 50 };

    assert.strictEqual(CollisionManager.checkAABB(boxA, boxTouchingRight), false);
  });
});
